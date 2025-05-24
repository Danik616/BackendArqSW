package com.konrad.backend.service;

import org.springframework.stereotype.Service;

import com.konrad.backend.model.Documento;
import com.konrad.backend.repository.DocumentoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;

    public DocumentoService(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public Flux<Documento> obtenerDocumentosPorSolicitudId(Long solicitudId) {
        return documentoRepository.findBySolicitudId(solicitudId);
    }

    public Mono<Documento> guardarDocumento(Documento documento) {
        return documentoRepository.save(documento);
    }

}
