import React, { useState } from 'react';
import api from '../api';

const VerDocumentos = () => {
  const [id, setId] = useState('');
  const [docs, setDocs] = useState([]);

  const buscarDocs = async () => {
    const res = await api.get(`/solicitudes/${id}/documentos`);
    setDocs(res.data);
  };

  return (
    <div>
      <h3>Documentos de una Solicitud</h3>
      <input value={id} onChange={e => setId(e.target.value)} placeholder="ID Solicitud" />
      <button onClick={buscarDocs}>Buscar</button>
      <ul>
        {docs.map((doc, i) => (
          <li key={i}>{doc.nombreArchivo || 'Documento sin nombre'}</li>
        ))}
      </ul>
    </div>
  );
};

export default VerDocumentos;