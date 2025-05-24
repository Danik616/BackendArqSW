package com.konrad.backend.service;

import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import com.konrad.backend.model.Solicitud;

public interface SolicitudValidator {
    String validate(Solicitud solicitud, Map<String, FilePart> archivos); // Retorna error o null si OK
}
