import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Home } from './pages/Home';
import { NuevaIniciativa } from './pages/NuevaIniciativa';
import { ListadoIniciativas } from './pages/ListadoIniciativas';
import { DetalleIniciativa } from './pages/DetalleIniciativa';
import { Evaluar } from './pages/Evaluar';
import { DetalleEvaluacion } from './pages/DetalleEvaluacion';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="iniciativas" element={<ListadoIniciativas />} />
        <Route path="iniciativas/nueva" element={<NuevaIniciativa />} />
        <Route path="iniciativas/:id" element={<DetalleIniciativa />} />
        <Route path="iniciativas/:id/evaluar" element={<Evaluar />} />
        <Route path="evaluaciones/:id" element={<DetalleEvaluacion />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
