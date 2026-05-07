import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { iniciativasApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';
import { EstadoBadge, ArquetipoBadge, ResultadoBadge } from '../components/EstadoBadge';

function formatFecha(s) {
  if (!s) return '—';
  return new Date(s).toLocaleString('es-AR');
}

function Campo({ label, children }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-0.5 text-sm whitespace-pre-wrap">{children || '—'}</p>
    </div>
  );
}

export function DetalleIniciativa() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [borrando, setBorrando] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    iniciativasApi
      .get(id)
      .then((r) => !cancelled && setData(r))
      .catch((e) => !cancelled && setError(e?.response?.data?.message || 'No se pudo cargar.'))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [id]);

  const eliminar = async () => {
    if (!confirm('¿Eliminar esta iniciativa? Se borrarán también sus evaluaciones.')) return;
    try {
      setBorrando(true);
      await iniciativasApi.remove(id);
      navigate('/iniciativas');
    } catch (e) {
      alert(e?.response?.data?.message || 'No se pudo eliminar.');
    } finally {
      setBorrando(false);
    }
  };

  if (loading) return <div className="p-6 text-sm text-slate-500">Cargando…</div>;
  if (error) return <div className="p-6 text-sm text-red-700">{error}</div>;
  if (!data) return null;

  const evaluaciones = data.evaluaciones || [];

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <Link to="/iniciativas" className="text-sm text-slate-600 hover:underline">
            ← Volver al listado
          </Link>
          <h1 className="mt-2 text-2xl font-bold">{data.titulo}</h1>
          <div className="mt-2 flex items-center gap-2">
            <EstadoBadge estado={data.estado} />
            <span className="text-xs text-slate-500">Creada {formatFecha(data.fechaCreacion)} por {data.usuarioCreador}</span>
          </div>
        </div>
        <div className="flex gap-2 shrink-0">
          <Button variant="secondary" onClick={eliminar} disabled={borrando}>
            {borrando ? 'Eliminando…' : 'Eliminar'}
          </Button>
          <Button onClick={() => navigate(`/iniciativas/${id}/evaluar`)}>+ Nueva evaluación</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Datos del intake</CardTitle>
        </CardHeader>
        <CardBody className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <Campo label="Área solicitante">{data.areaSolicitante}</Campo>
          <Campo label="Responsable">{data.responsable}</Campo>
          <Campo label="Sponsor ejecutivo">{data.sponsorEjecutivo}</Campo>
          <div className="md:col-span-3">
            <Campo label="Descripción del problema">{data.descripcionProblema}</Campo>
          </div>
          <div className="md:col-span-3">
            <Campo label="Descripción de la solución">{data.descripcionSolucion}</Campo>
          </div>
          <div className="md:col-span-3 grid grid-cols-1 md:grid-cols-2 gap-5">
            <Campo label="Impacto esperado">{data.impactoEsperado}</Campo>
            <Campo label="Datos disponibles">{data.datosDisponibles}</Campo>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Historial de evaluaciones</CardTitle>
            <span className="text-xs text-slate-500">{evaluaciones.length} en total</span>
          </div>
        </CardHeader>
        {evaluaciones.length === 0 ? (
          <CardBody>
            <p className="text-sm text-slate-500">
              Esta iniciativa todavía no fue evaluada.{' '}
              <Link className="text-brand-700 underline" to={`/iniciativas/${id}/evaluar`}>
                Iniciar primera evaluación
              </Link>
            </p>
          </CardBody>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500 border-b">
                <tr>
                  <th className="px-4 py-3 font-medium">Fecha</th>
                  <th className="px-4 py-3 font-medium">Evaluador</th>
                  <th className="px-4 py-3 font-medium">Matriz</th>
                  <th className="px-4 py-3 font-medium">Puntaje</th>
                  <th className="px-4 py-3 font-medium">Arquetipo</th>
                  <th className="px-4 py-3 font-medium">Resultado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {evaluaciones.map((e) => (
                  <tr
                    key={e.id}
                    className="hover:bg-slate-50 cursor-pointer"
                    onClick={() => navigate(`/evaluaciones/${e.id}`)}
                  >
                    <td className="px-4 py-3">{formatFecha(e.fechaEvaluacion)}</td>
                    <td className="px-4 py-3">{e.usuarioEvaluador}</td>
                    <td className="px-4 py-3 text-slate-700">{e.nombreMatriz}</td>
                    <td className="px-4 py-3 font-mono font-semibold">{Number(e.puntajeTotal).toFixed(2)}</td>
                    <td className="px-4 py-3">
                      <ArquetipoBadge arquetipo={e.arquetipo} />
                      {e.tieneVeto && <span className="ml-2 text-xs font-semibold text-red-700">VETADA</span>}
                    </td>
                    <td className="px-4 py-3">
                      <ResultadoBadge resultado={e.resultado} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
