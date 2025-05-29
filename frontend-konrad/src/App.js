import React from 'react';
import CrearSolicitud from './components/CrearSolicitud';
import ConsultarSolicitud from './components/ConsultarSolicitud';
import ListarSolicitudes from './components/ListarSolicitudes';
import VerDocumentos from './components/VerDocumentos';

function App() {
  return (
    <div className="container">
      <h1>Gestión de Solicitudes</h1>
      <CrearSolicitud />
      <ConsultarSolicitud />
      <ListarSolicitudes />
      <VerDocumentos />
    </div>
  );
}

export default App;