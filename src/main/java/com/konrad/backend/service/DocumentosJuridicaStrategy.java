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
public class DocumentosJuridicaStrategy implements DocumentosStrategy {
    @Override
    public TipoPersona getTipoPersona() {
        return TipoPersona.JURIDICA;
    }

    @Override
    public Mono<List<Documento>> procesarDocumentos(Solicitud solicitud, Map<String, FilePart> archivos) {
        // Esperados: fotocopia_nit, formato_aceptacion_consulta,
        // formato_aceptacion_tratamiento, rut, camara_comercio
        List<String> requiredDocs = List.of(
                "fotocopia_nit",
                "formato_aceptacion_consulta",
                "formato_aceptacion_tratamiento",
                "rut",
                "camara_comercio");

        List<Mono<Documento>> documentosMono = requiredDocs.stream()
                .<Mono<Documento>>map(docName -> {
                    FilePart filePart = archivos.get(docName);
                    if (filePart == null) {
                        return Mono.error(new RuntimeException("Falta el documento: " + docName));
                    }
                    String basePath = System.getProperty("java.io.tmpdir");
                    java.nio.file.Path destinoPath = java.nio.file.Paths.get(basePath, "uploads",
                            java.util.UUID.randomUUID() + "-" + filePart.filename());
                    String rutaDestino = destinoPath.toString();

                    // Crear el directorio si no existe
                    try {
                        java.nio.file.Files.createDirectories(destinoPath.getParent());
                    } catch (java.io.IOException e) {
                        return Mono.error(e);
                    }
                    Mono<Void> guardado = filePart.transferTo(destinoPath);
                    return guardado.thenReturn(new Documento(null, docName, rutaDestino, null));
                })
                .toList();

        return reactor.core.publisher.Flux.concat(documentosMono).collectList();
    }

}
