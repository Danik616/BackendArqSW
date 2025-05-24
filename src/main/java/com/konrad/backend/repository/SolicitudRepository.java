package com.konrad.backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.konrad.backend.model.Solicitud;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository extends R2dbcRepository<Solicitud, Long> {
    Mono<Solicitud> findByNumeroIdentificacion(String numeroIdentificacion);

    @Query("""
                SELECT * FROM solicitudes
                WHERE (:numeroIdentificacion IS NULL OR numero_identificacion = :numeroIdentificacion)
                  AND (:estado IS NULL OR estado_solicitud = :estado)
                  AND (:fechaInicio IS NULL OR fecha_creacion >= :fechaInicio)
                  AND (:fechaFin IS NULL OR fecha_creacion <= :fechaFin)
                ORDER BY fecha_creacion DESC
            """)
    Flux<Solicitud> findByFiltros(String numeroIdentificacion, String estado, LocalDateTime fechaInicio,
            LocalDateTime fechaFin);
}
