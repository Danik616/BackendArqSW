package com.konrad.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.konrad.backend.model.Documento;
import com.konrad.backend.model.Solicitud;
import com.konrad.backend.model.TipoPersona;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SolicitudFacadeService {

    private final SolicitudService solicitudService;
    private final DocumentoService documentoService;
    private final List<SolicitudValidator> validators; // Chain of Responsibility
    private final Map<TipoPersona, DocumentosStrategy> documentosStrategyMap; // Strategy

    public SolicitudFacadeService(
            SolicitudService solicitudService,
            DocumentoService documentoService,
            List<SolicitudValidator> validators,
            List<DocumentosStrategy> strategies) {
        this.solicitudService = solicitudService;
        this.documentoService = documentoService;
        this.validators = validators;
        this.documentosStrategyMap = strategies.stream()
                .collect(Collectors.toMap(DocumentosStrategy::getTipoPersona, s -> s));
    }

    public Mono<Solicitud> procesarSolicitud(Solicitud solicitud, Map<String, FilePart> archivos) {
        // 1. Ejecuta la cadena de validaciones (Chain of Responsibility)
        for (SolicitudValidator validator : validators) {
            String error = validator.validate(solicitud, archivos);
            if (error != null) {
                return Mono.error(new RuntimeException(error));
            }
        }

        // 2. Usa Strategy para validar/guardar los documentos según tipo persona
        DocumentosStrategy strategy = documentosStrategyMap.get(solicitud.getTipoPersona());
        if (strategy == null) {
            return Mono.error(new RuntimeException("Tipo de persona inválido"));
        }
        return strategy.procesarDocumentos(solicitud, archivos)
                .flatMap(listaDocs -> solicitudService.crearSolicitud(solicitud)
                        .flatMap(solicitudGuardada -> {
                            List<Mono<Documento>> docsConId = listaDocs.stream().map(doc -> {
                                doc.setSolicitudId(solicitudGuardada.getId());
                                return documentoService.guardarDocumento(doc);
                            }).toList();
                            return Flux.concat(docsConId).then(Mono.just(solicitudGuardada));
                        }));
    }
}
