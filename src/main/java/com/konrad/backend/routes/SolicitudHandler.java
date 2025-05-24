
package com.konrad.backend.routes;

import com.konrad.backend.model.EstadoSolicitud;
import com.konrad.backend.model.Solicitud;
import com.konrad.backend.model.TipoPersona;
import com.konrad.backend.service.SolicitudService;
import com.konrad.backend.service.DocumentoService;
import com.konrad.backend.service.SolicitudFacadeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class SolicitudHandler {

    private final SolicitudService solicitudService;
    private final DocumentoService documentoService;

    @Autowired
    private SolicitudFacadeService solicitudFacadeService;

    public SolicitudHandler(SolicitudService solicitudService,
            DocumentoService documentoService) {
        this.documentoService = documentoService;
        this.solicitudService = solicitudService;
    }

    public Mono<ServerResponse> crearSolicitud(ServerRequest request) {
        // Extrae los query params
        String nombres = request.queryParam("nombres").orElse("");
        String apellidos = request.queryParam("apellidos").orElse("");
        String numeroIdentificacion = request.queryParam("numeroIdentificacion").orElse("");
        String correoElectronico = request.queryParam("correoElectronico").orElse("");
        String paisResidencia = request.queryParam("paisResidencia").orElse("");
        String ciudadResidencia = request.queryParam("ciudadResidencia").orElse("");
        String telefono = request.queryParam("telefono").orElse("");
        String tipoIdentificacion = request.queryParam("tipoIdentificacion").orElse("").toUpperCase();

        TipoPersona tipoPersona;
        if ("CEDULA".equals(tipoIdentificacion)) {
            tipoPersona = TipoPersona.NATURAL;
        } else if ("NIT".equals(tipoIdentificacion)) {
            tipoPersona = TipoPersona.JURIDICA;
        } else {
            return ServerResponse.badRequest().bodyValue("tipoIdentificacion inválido, debe ser CEDULA o NIT");
        }

        // Recibe los archivos del multipart
        return request.multipartData()
                .flatMap(parts -> {
                    // Convierte MultiValueMap<String, Part> a Map<String, FilePart>
                    Map<String, FilePart> archivos = parts.entrySet().stream()
                            .filter(e -> e.getValue().get(0) instanceof FilePart)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> (FilePart) e.getValue().get(0)));

                    Solicitud solicitud = new Solicitud();
                    solicitud.setNombres(nombres);
                    solicitud.setApellidos(apellidos);
                    solicitud.setNumeroIdentificacion(numeroIdentificacion);
                    solicitud.setCorreoElectronico(correoElectronico);
                    solicitud.setPaisResidencia(paisResidencia);
                    solicitud.setCiudadResidencia(ciudadResidencia);
                    solicitud.setTelefono(telefono);
                    solicitud.setTipoPersona(tipoPersona);
                    solicitud.setFechaCreacion(java.time.LocalDateTime.now());
                    solicitud.setEstado(com.konrad.backend.model.EstadoSolicitud.PENDIENTE);

                    return solicitudFacadeService.procesarSolicitud(solicitud, archivos)
                            .flatMap(solicitudGuardada -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitudGuardada))
                            .onErrorResume(e -> {
                                if (e instanceof DuplicateKeyException || (e.getCause() != null && e.getCause()
                                        .getClass().getSimpleName().contains("R2dbcDataIntegrityViolationException"))) {
                                    return ServerResponse.status(409)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(Map.of(
                                                    "error", "Ya existe una solicitud con ese número de identificación",
                                                    "codigo", "DUPLICATE_NUMERO_IDENTIFICACION"));
                                }
                                return ServerResponse.status(400)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of("error", e.getMessage()));
                            });
                });
    }

    public Mono<ServerResponse> consultarSolicitudPorId(ServerRequest request) {
        Long id;
        try {
            id = Long.parseLong(request.pathVariable("id"));
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("El id debe ser un número");
        }

        return solicitudService.obtenerPorId(id)
                .flatMap(solicitud -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitud))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listarSolicitudes(ServerRequest request) {
        String numeroIdentificacion = request.queryParam("numeroIdentificacion").orElse(null);
        String estadoStr = request.queryParam("estado").orElse(null);
        String fechaInicioStr = request.queryParam("fechaInicio").orElse(null);
        String fechaFinStr = request.queryParam("fechaFin").orElse(null);

        EstadoSolicitud estado = null;
        if (estadoStr != null) {
            try {
                estado = EstadoSolicitud.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ServerResponse.badRequest()
                        .bodyValue("Estado inválido. Valores permitidos: " + Arrays.toString(EstadoSolicitud.values()));
            }
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE; // Solo yyyy-MM-dd
        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        try {
            if (fechaInicioStr != null) {
                // Parseamos fechaInicio y le ponemos hora 00:00:00
                fechaInicio = LocalDateTime.of(LocalDate.parse(fechaInicioStr, dateFormatter), LocalTime.MIN);
            }
            if (fechaFinStr != null) {

                fechaFin = LocalDateTime.of(LocalDate.parse(fechaFinStr, dateFormatter), LocalTime.MAX);
            }
        } catch (Exception e) {
            return ServerResponse.badRequest()
                    .bodyValue("Formato de fecha inválido, usar yyyy-MM-dd");
        }

        return solicitudService.listarSolicitudesFiltradas(numeroIdentificacion, estado, fechaInicio, fechaFin)
                .collectList()
                .flatMap(lista -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(lista));
    }

    public Mono<ServerResponse> obtenerDocumentosPorSolicitudId(ServerRequest request) {
        Long solicitudId;
        try {
            solicitudId = Long.parseLong(request.pathVariable("id"));
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("El id debe ser un número");
        }
        return documentoService.obtenerDocumentosPorSolicitudId(solicitudId)
                .collectList()
                .flatMap(documentos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(documentos));
    }

}
