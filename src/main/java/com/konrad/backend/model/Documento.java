package com.konrad.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("documentos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Documento {

    @Id
    private Long id;

    private String tipo;

    @Column("ruta_archivo")
    private String rutaArchivo;

    @Column("solicitud_id")
    private Long solicitudId; // FK manual para relación

    // getters y setters
}
