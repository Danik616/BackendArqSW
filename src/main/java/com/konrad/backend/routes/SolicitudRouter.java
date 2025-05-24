package com.konrad.backend.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class SolicitudRouter {

    @Bean
    public RouterFunction<ServerResponse> rutaSolicitudes(SolicitudHandler handler) {
        return RouterFunctions.route()
                .POST("/solicitudes", handler::crearSolicitud)
                .GET("/solicitudes/{id}", handler::consultarSolicitudPorId)
                .GET("/solicitudes/{id}/documentos", handler::obtenerDocumentosPorSolicitudId)
                .GET("/solicitudes", handler::listarSolicitudes)
                .build();
    }
}