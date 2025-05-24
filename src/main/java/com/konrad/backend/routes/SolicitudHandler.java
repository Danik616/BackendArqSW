
package com.konrad.backend.routes;

import com.konrad.backend.model.Documento;
import com.konrad.backend.model.EstadoSolicitud;
import com.konrad.backend.model.Solicitud;
import com.konrad.backend.model.TipoPersona;
import com.konrad.backend.service.SolicitudService;
import com.konrad.backend.service.DocumentoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SolicitudHandler {

    private final SolicitudService solicitudService;
    private final DocumentoService documentoService;

    public SolicitudHandler(SolicitudService solicitudService,
            DocumentoService documentoService) {
        this.documentoService = documentoService;
        this.solicitudService = solicitudService;
    }

    public Mono<ServerResponse> crearSolicitud(ServerRequest request) {
        // Leer query params
        String nombres = request.queryParam("nombres").orElse("");
        String apellidos = request.queryParam("apellidos").orElse("");
        String numeroIdentificacion = request.queryParam("numeroIdentificacion").orElse("");
        String correoElectronico = request.queryParam("correoElectronico").orElse("");
        String paisResidencia = request.queryParam("paisResidencia").orElse("");
        String ciudadResidencia = request.queryParam("ciudadResidencia").orElse("");
        String telefono = request.queryParam("telefono").orElse("");
        String tipoIdentificacion = request.queryParam("tipoIdentificacion").orElse("").toUpperCase();

        // Validar tipoIdentificacion
        TipoPersona tipoPersona;
        if ("CEDULA".equals(tipoIdentificacion)) {
            tipoPersona = TipoPersona.NATURAL;
        } else if ("NIT".equals(tipoIdentificacion)) {
            tipoPersona = TipoPersona.JURIDICA;
        } else {
            return ServerResponse.badRequest()
                    .bodyValue("tipoIdentificacion inválido, debe ser CEDULA o NIT");
        }

        // Leer multipart data (archivos)
        return request.multipartData()
                .flatMap(parts -> {
                    // Archivos requeridos según tipoPersona
                    List<String> requiredDocs = tipoPersona == TipoPersona.NATURAL
                            ? List.of("fotocopia_cedula", "formato_aceptacion_consulta",
                                    "formato_aceptacion_tratamiento")
                            : List.of("fotocopia_nit", "formato_aceptacion_consulta", "formato_aceptacion_tratamiento",
                                    "rut", "camara_comercio");

                    // Validar archivos presentes
                    Set<String> archivosRecibidos = parts.keySet();
                    List<String> faltantes = requiredDocs.stream()
                            .filter(doc -> !archivosRecibidos.contains(doc))
                            .toList();
                    if (!faltantes.isEmpty()) {
                        return ServerResponse.badRequest()
                                .bodyValue("Faltan documentos obligatorios: " + String.join(", ", faltantes));
                    }

                    // Crear solicitud sin documentos (guardaremos luego)
                    Solicitud solicitud = new Solicitud();
                    solicitud.setNombres(nombres);
                    solicitud.setApellidos(apellidos);
                    solicitud.setNumeroIdentificacion(numeroIdentificacion);
                    solicitud.setCorreoElectronico(correoElectronico);
                    solicitud.setPaisResidencia(paisResidencia);
                    solicitud.setCiudadResidencia(ciudadResidencia);
                    solicitud.setTelefono(telefono);
                    solicitud.setTipoPersona(tipoPersona);
                    solicitud.setFechaCreacion(LocalDateTime.now());
                    solicitud.setEstado(EstadoSolicitud.PENDIENTE);

                    // Guardar archivos (reactivo) y crear lista Mono<Documento>
                    List<Mono<Documento>> documentosMono = requiredDocs.stream()
                            .<Mono<Documento>>map(docName -> {
                                FilePart filePart = (FilePart) parts.getFirst(docName);

                                String basePath = System.getProperty("java.io.tmpdir"); // Ruta temporal OS
                                                                                        // independiente
                                java.nio.file.Path destinoPath = Paths
                                        .get(basePath, "uploads", UUID.randomUUID() + "-" + filePart.filename());
                                String rutaDestino = destinoPath.toString();

                                try {
                                    if (!Files.exists(destinoPath.getParent())) {
                                        Files.createDirectories(destinoPath.getParent());
                                    }
                                } catch (IOException e) {
                                    return Mono.error(e);
                                }

                                Mono<Void> guardado = filePart.transferTo(destinoPath);
                                // Crear objeto Documento con ruta y tipo, solicitudId lo asignamos después
                                return guardado.thenReturn(new Documento(null, docName, rutaDestino, null));
                            })
                            .toList();

                    // Primero guardamos los archivos y recolectamos documentos
                    return Flux.concat(documentosMono)
                            .collectList()
                            .flatMap(documentosGuardados ->
                    // Guardamos la solicitud para obtener id
                    solicitudService.crearSolicitud(solicitud)
                            .flatMap(solicitudGuardada -> {
                                // Asignamos solicitudId a cada documento y guardamos
                                List<Mono<Documento>> docsGuardadosConId = documentosGuardados.stream()
                                        .map(doc -> {
                                            doc.setSolicitudId(solicitudGuardada.getId());
                                            return documentoService.guardarDocumento(doc);
                                        })
                                        .toList();

                                // Guardar todos los documentos y devolver la solicitud
                                return Flux.concat(docsGuardadosConId)
                                        .then(Mono.just(solicitudGuardada));
                            }))
                            .flatMap(solicitudGuardada -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitudGuardada));
                })
                .onErrorResume(e -> {

                    if (e instanceof org.springframework.dao.DuplicateKeyException
                            || (e.getCause() != null
                                    && e.getCause() instanceof io.r2dbc.spi.R2dbcDataIntegrityViolationException)) {
                        return ServerResponse.status(409) // Conflict
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(
                                        Map.of("error", "Número de identificación duplicado",
                                                "message", "Ya existe una solicitud con ese número de identificación"));
                    }
                    // Manejo general para otros errores
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(
                                    Map.of("error", "Error interno del servidor",
                                            "message", e.getMessage()));
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
