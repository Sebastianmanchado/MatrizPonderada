import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { iniciativasApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';
import { EstadoBadge, ArquetipoBadge, ResultadoBadge } from '../components/EstadoBadge';
import { NuevaVersionForm } from '../components/NuevaVersionForm';

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

function VersionBadge({ numero, esActual }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ${
        esActual ? 'bg-brand-100 text-brand-800' : 'bg-slate-100 text-slate-700'
      }`}
    >
      v{numero}
      {esActual && <span className="ml-1 text-[10px] uppercase">actual</span>}
    </span>
  );
}

export function DetalleIniciativa() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [borrando, setBorrando] = useState(false);
  const [mostrandoFormVersion, setMostrandoFormVersion] = useState(false);
  const [versionExpandida, setVersionExpandida] = useState(null);

  const cargar = () => {
    setLoading(true);
    return iniciativasApi
      .get(id)
      .then(setData)
      .catch((e) => setError(e?.response?.data?.message || 'No se pudo cargar.'))
      .finally(() => setLoading(false));
  };

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
    if (!confirm('¿Eliminar esta iniciativa? Se borrarán también sus versiones y evaluaciones.')) return;
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
  const versiones = data.versiones || [];

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <Link to="/iniciativas" className="text-sm text-slate-600 hover:underline">
            ← Volver al listado
          </Link>
          <div className="mt-2 flex items-center gap-3 flex-wrap">
            <h1 className="text-2xl font-bold">{data.titulo}</h1>
            <VersionBadge numero={data.numeroVersionActual} esActual />
          </div>
          <div className="mt-2 flex items-center gap-2">
            <EstadoBadge estado={data.estado} />
            <span className="text-xs text-slate-500">
              Creada {formatFecha(data.fechaCreacion)} por {data.usuarioCreador}
            </span>
          </div>
        </div>
        <div className="flex gap-2 shrink-0">
          <Button variant="secondary" onClick={eliminar} disabled={borrando}>
            {borrando ? 'Eliminando…' : 'Eliminar'}
          </Button>
          <Button variant="secondary" onClick={() => setMostrandoFormVersion(true)}>
            + Nueva versión
          </Button>
          <Button onClick={() => navigate(`/iniciativas/${id}/evaluar`)}>+ Nueva evaluación</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Datos del intake (versión actual)</CardTitle>
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

      {mostrandoFormVersion && (
        <Card>
          <CardHeader>
            <CardTitle>Crear nueva versión</CardTitle>
          </CardHeader>
          <CardBody>
            <NuevaVersionForm
              idIniciativa={id}
              base={data}
              onCancel={() => setMostrandoFormVersion(false)}
              onCreated={async () => {
                setMostrandoFormVersion(false);
                await cargar();
              }}
            />
          </CardBody>
        </Card>
      )}

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Historial de versiones</CardTitle>
            <span className="text-xs text-slate-500">{versiones.length} en total</span>
          </div>
        </CardHeader>
        <CardBody className="space-y-2">
          {versiones.length === 0 ? (
            <p className="text-sm text-slate-500">Sin versiones registradas.</p>
          ) : (
            versiones.map((v) => {
              const expandida = versionExpandida === v.id;
              return (
                <div
                  key={v.id}
                  className={`rounded-md border p-3 ${
                    v.esActual ? 'border-brand-300 bg-brand-50/40' : 'border-slate-200 bg-white'
                  }`}
                >
                  <button
                    type="button"
                    className="flex w-full items-start justify-between gap-3 text-left"
                    onClick={() => setVersionExpandida(expandida ? null : v.id)}
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <VersionBadge numero={v.numeroVersion} esActual={v.esActual} />
                        <span className="text-sm font-medium text-slate-900">{v.titulo}</span>
                      </div>
                      <p className="mt-1 text-xs text-slate-500">
                        {formatFecha(v.fechaVersion)} · {v.usuarioVersion}
                        {v.comentarioVersion && (
                          <>
                            {' · '}
                            <span className="italic">{v.comentarioVersion}</span>
                          </>
                        )}
                      </p>
                    </div>
                    <span className="text-xs text-slate-400 shrink-0 mt-1">{expandida ? '▴' : '▾'}</span>
                  </button>
                  {expandida && (
                    <div className="mt-3 pt-3 border-t grid grid-cols-1 md:grid-cols-3 gap-4">
                      <Campo label="Área">{v.areaSolicitante}</Campo>
                      <Campo label="Responsable">{v.responsable}</Campo>
                      <Campo label="Sponsor">{v.sponsorEjecutivo}</Campo>
                      <div className="md:col-span-3">
                        <Campo label="Problema">{v.descripcionProblema}</Campo>
                      </div>
                      <div className="md:col-span-3">
                        <Campo label="Solución">{v.descripcionSolucion}</Campo>
                      </div>
                      <Campo label="Impacto esperado">{v.impactoEsperado}</Campo>
                      <Campo label="Datos disponibles">{v.datosDisponibles}</Campo>
                    </div>
                  )}
                </div>
              );
            })
          )}
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
                  <th className="px-4 py-3 font-medium">Versión</th>
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
                    <td className="px-4 py-3">
                      {e.numeroVersionIniciativa != null ? (
                        <VersionBadge
                          numero={e.numeroVersionIniciativa}
                          esActual={e.numeroVersionIniciativa === data.numeroVersionActual}
                        />
                      ) : (
                        '—'
                      )}
                    </td>
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
