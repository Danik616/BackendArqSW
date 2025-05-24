package com.konrad.backend.service;

import com.konrad.backend.model.Solicitud;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.konrad.backend.model.EstadoSolicitud;
import com.konrad.backend.repository.SolicitudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public Mono<Solicitud> crearSolicitud(Solicitud solicitud) {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

    public Mono<Solicitud> obtenerPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Mono<Solicitud> actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        return solicitudRepository.findById(id)
                .flatMap(solicitud -> {
                    solicitud.setEstado(nuevoEstado);
                    return solicitudRepository.save(solicitud);
                });
    }

    public Flux<Solicitud> listarSolicitudesFiltradas(String numeroIdentificacion, EstadoSolicitud estado,
            LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String estadoStr = estado != null ? estado.name() : null;
        return solicitudRepository.findByFiltros(numeroIdentificacion, estadoStr, fechaInicio, fechaFin);
    }
}
