package com.konrad.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("solicitudes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Solicitud {

    @Id
    private Long id;

    private String nombres;
    private String apellidos;
    @Column("numero_identificacion")
    private String numeroIdentificacion;
    @Column("correo_electronico")
    private String correoElectronico;
    @Column("pais_residencia")
    private String paisResidencia;
    @Column("ciudad_residencia")
    private String ciudadResidencia;
    private String telefono;

    @Column("tipo_persona")
    private TipoPersona tipoPersona;

    @Column("estado_solicitud")
    private EstadoSolicitud estado;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column("fecha_respuesta")
    private LocalDateTime fechaRespuesta;

}
