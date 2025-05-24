package com.konrad.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import com.konrad.backend.model.Documento;
import com.konrad.backend.model.Solicitud;
import com.konrad.backend.model.TipoPersona;

import reactor.core.publisher.Mono;

@Component
public class DocumentosNaturalStrategy implements DocumentosStrategy {
    @Override
    public TipoPersona getTipoPersona() {
        return TipoPersona.NATURAL;
    }

    @Override
    public Mono<List<Documento>> procesarDocumentos(Solicitud solicitud, Map<String, FilePart> archivos) {
        List<String> requiredDocs = List.of(
                "fotocopia_cedula",
                "formato_aceptacion_consulta",
                "formato_aceptacion_tratamiento");

        List<Mono<Documento>> documentosMono = requiredDocs.stream()
                .map(docName -> {
                    FilePart filePart = archivos.get(docName);
                    if (filePart == null) {
                        return Mono.<Documento>error(new RuntimeException("Falta el documento: " + docName));
                    }
                    String basePath = System.getProperty("java.io.tmpdir");
                    java.nio.file.Path destinoPath = java.nio.file.Paths.get(basePath, "uploads",
                            java.util.UUID.randomUUID() + "-" + filePart.filename());
                    String rutaDestino = destinoPath.toString();

                    try {
                        java.nio.file.Files.createDirectories(destinoPath.getParent());
                    } catch (java.io.IOException e) {
                        return Mono.<Documento>error(e);
                    }
                    Mono<Void> guardado = filePart.transferTo(destinoPath);
                    return guardado.thenReturn(new Documento(null, docName, rutaDestino, null));
                })
                .toList();

        return reactor.core.publisher.Flux.concat(documentosMono).collectList();
    }

}
