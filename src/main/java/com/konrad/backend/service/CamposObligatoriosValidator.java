package com.konrad.backend.service;

import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import com.konrad.backend.model.Solicitud;

@Component
public class CamposObligatoriosValidator implements SolicitudValidator {
    @Override
    public String validate(Solicitud solicitud, Map<String, FilePart> archivos) {
        if (solicitud.getNombres() == null || solicitud.getNombres().isEmpty())
            return "El nombre es obligatorio";
        if (solicitud.getNumeroIdentificacion() == null || solicitud.getNumeroIdentificacion().isEmpty())
            return "El número de identificación es obligatorio";
        return null;
    }
}
