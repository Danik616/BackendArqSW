import React, { useState } from 'react';
import api from '../api';

const ConsultarSolicitud = () => {
  const [id, setId] = useState('');
  const [solicitud, setSolicitud] = useState(null);

  const handleBuscar = async () => {
    try {
      const res = await api.get(`/solicitudes/${id}`);
      setSolicitud(res.data);
    } catch {
      setSolicitud(null);
    }
  };

  return (
    <div>
      <h3>Consultar Solicitud por ID</h3>
      <input value={id} onChange={e => setId(e.target.value)} placeholder="ID" />
      <button onClick={handleBuscar}>Buscar</button>
      {solicitud && <pre>{JSON.stringify(solicitud, null, 2)}</pre>}
    </div>
  );
};

export default ConsultarSolicitud;