import React, { useState } from 'react';
import api from '../api';

const ListarSolicitudes = () => {
  const [filtros, setFiltros] = useState({});
  const [resultado, setResultado] = useState([]);

  const handleChange = e => setFiltros({ ...filtros, [e.target.name]: e.target.value });

  const buscar = async () => {
    const params = new URLSearchParams(filtros).toString();
    const res = await api.get(`/solicitudes?${params}`);
    setResultado(res.data);
  };

  return (
    <div>
      <h3>Filtrar Solicitudes</h3>
      <input name="numeroIdentificacion" placeholder="ID" onChange={handleChange} />
      <input name="estado" placeholder="Estado (PENDIENTE, APROBADA...)" onChange={handleChange} />
      <input name="fechaInicio" placeholder="Desde (yyyy-mm-dd)" onChange={handleChange} />
      <input name="fechaFin" placeholder="Hasta (yyyy-mm-dd)" onChange={handleChange} />
      <button onClick={buscar}>Buscar</button>

      <ul>
        {resultado.map(s => (
          <li key={s.id}>{s.nombres} {s.apellidos} ({s.estado})</li>
        ))}
      </ul>
    </div>
  );
};

export default ListarSolicitudes;