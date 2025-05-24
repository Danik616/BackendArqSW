package com.konrad.backend.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.konrad.backend.model.Documento;

import reactor.core.publisher.Flux;

public interface DocumentoRepository extends R2dbcRepository<Documento, Long> {
    Flux<Documento> findBySolicitudId(Long solicitudId);
}
