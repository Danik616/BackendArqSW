import React, { useState } from 'react';
import api from '../api';

const CrearSolicitud = () => {
  const [formData, setFormData] = useState({
    nombres: '',
    apellidos: '',
    numeroIdentificacion: '',
    correoElectronico: '',
    paisResidencia: '',
    ciudadResidencia: '',
    telefono: '',
    tipoIdentificacion: 'CEDULA'
  });

  const [files, setFiles] = useState({});
  const [mensaje, setMensaje] = useState('');

  const handleInputChange = e => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleFileChange = e => {
    setFiles({ ...files, [e.target.name]: e.target.files[0] });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    const data = new FormData();
    Object.entries(formData).forEach(([key, value]) => data.append(key, value));
    Object.entries(files).forEach(([key, value]) => data.append(key, value));

    try {
      const res = await api.post('/solicitudes', data);
      setMensaje('Solicitud creada con ID: ' + res.data.id);
    } catch (err) {
      setMensaje('Error: ' + (err.response?.data?.error || err.message));
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>Crear Solicitud</h3>
      <input name="nombres" placeholder="Nombres" onChange={handleInputChange} required />
      <input name="apellidos" placeholder="Apellidos" onChange={handleInputChange} required />
      <input name="numeroIdentificacion" placeholder="Número de ID" onChange={handleInputChange} required />
      <input name="correoElectronico" placeholder="Correo" onChange={handleInputChange} required />
      <input name="paisResidencia" placeholder="País" onChange={handleInputChange} required />
      <input name="ciudadResidencia" placeholder="Ciudad" onChange={handleInputChange} required />
      <input name="telefono" placeholder="Teléfono" onChange={handleInputChange} required />
      <select name="tipoIdentificacion" onChange={handleInputChange}>
        <option value="CEDULA">Cédula</option>
        <option value="NIT">NIT</option>
      </select>
      <input type="file" name="documento1" onChange={handleFileChange} />
      <button type="submit">Enviar</button>
      {mensaje && <p>{mensaje}</p>}
    </form>
  );
};

export default CrearSolicitud;