package com.konrad.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import com.konrad.backend.model.Documento;
import com.konrad.backend.model.Solicitud;
import com.konrad.backend.model.TipoPersona;

import reactor.core.publisher.Mono;

public interface DocumentosStrategy {
    TipoPersona getTipoPersona();

    Mono<List<Documento>> procesarDocumentos(Solicitud solicitud, Map<String, FilePart> archivos);
}
