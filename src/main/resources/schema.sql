CREATE TABLE solicitudes (
    id SERIAL PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    numero_identificacion VARCHAR(50) UNIQUE,
    correo_electronico VARCHAR(100),
    pais_residencia VARCHAR(100),
    ciudad_residencia VARCHAR(100),
    telefono VARCHAR(50),
    tipo_persona VARCHAR(20),
    estado_solicitud VARCHAR(20),
    fecha_creacion TIMESTAMP,
    fecha_respuesta TIMESTAMP
);

CREATE TABLE documentos (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(100),
    ruta_archivo VARCHAR(255),
    solicitud_id BIGINT REFERENCES solicitudes(id)
);
