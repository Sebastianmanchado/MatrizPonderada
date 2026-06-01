import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { evaluacionesApi } from '../api/client';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';
import { ArquetipoBadge, ResultadoBadge } from '../components/EstadoBadge';

function formatFecha(s) {
  if (!s) return '—';
  return new Date(s).toLocaleString('es-AR');
}

export function DetalleEvaluacion() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    evaluacionesApi
      .get(id)
      .then((r) => !cancelled && setData(r))
      .catch((e) => !cancelled && setError(e?.response?.data?.message || 'No se pudo cargar.'))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [id]);

  if (loading) return <div className="p-6 text-sm text-slate-500">Cargando…</div>;
  if (error) return <div className="p-6 text-sm text-red-700">{error}</div>;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <div>
        <Link to={`/iniciativas/${data.idIniciativa}`} className="text-sm text-slate-600 hover:underline">
          ← Volver a la iniciativa
        </Link>
        <h1 className="mt-2 text-2xl font-bold">Evaluación #{data.id}</h1>
        <p className="mt-1 text-sm text-slate-600">
          Iniciativa: <span className="font-medium">{data.tituloIniciativa}</span>
          {data.iniciativaVersion && (
            <>
              {' '}·{' '}
              <span
                className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ${
                  data.iniciativaVersion.esActual ? 'bg-brand-100 text-brand-800' : 'bg-slate-100 text-slate-700'
                }`}
              >
                v{data.iniciativaVersion.numeroVersion}
                {data.iniciativaVersion.esActual && <span className="ml-1 text-[10px] uppercase">actual</span>}
              </span>
              {!data.iniciativaVersion.esActual && (
                <span className="ml-2 text-xs text-amber-700">
                  (versión histórica — la iniciativa siguió evolucionando)
                </span>
              )}
            </>
          )}
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Resumen</CardTitle>
        </CardHeader>
        <CardBody className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <p className="text-xs text-slate-500">Puntaje</p>
            <p className="mt-1 font-mono text-2xl font-bold">{Number(data.puntajeTotal).toFixed(2)}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500">Arquetipo</p>
            <div className="mt-2"><ArquetipoBadge arquetipo={data.arquetipo} /></div>
          </div>
          <div>
            <p className="text-xs text-slate-500">Resultado</p>
            <div className="mt-2"><ResultadoBadge resultado={data.resultado} /></div>
          </div>
          <div>
            <p className="text-xs text-slate-500">Veto</p>
            <p className="mt-1 text-sm font-semibold">{data.tieneVeto ? 'SÍ — descarta' : 'No'}</p>
          </div>
          <div className="col-span-2 md:col-span-4 grid grid-cols-2 gap-4 pt-2 border-t">
            <div>
              <p className="text-xs text-slate-500">Fecha</p>
              <p className="mt-1 text-sm">{formatFecha(data.fechaEvaluacion)}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">Evaluador</p>
              <p className="mt-1 text-sm">{data.usuarioEvaluador}</p>
            </div>
          </div>
          {data.notas && (
            <div className="col-span-2 md:col-span-4">
              <p className="text-xs text-slate-500">Notas</p>
              <p className="mt-1 text-sm whitespace-pre-wrap">{data.notas}</p>
            </div>
          )}
        </CardBody>
      </Card>

      {data.iniciativaVersion && (
        <Card>
          <CardHeader>
            <CardTitle>
              Snapshot de la iniciativa (v{data.iniciativaVersion.numeroVersion})
            </CardTitle>
            <p className="mt-1 text-xs text-slate-500">
              Contenido de la iniciativa al momento de evaluarla.
              {data.iniciativaVersion.comentarioVersion && (
                <> Comentario: <em>{data.iniciativaVersion.comentarioVersion}</em>.</>
              )}
            </p>
          </CardHeader>
          <CardBody className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div>
              <p className="text-xs text-slate-500">Area solicitante</p>
              <p>{data.iniciativaVersion.areaSolicitante}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">Nombre y cargo</p>
              <p>{data.iniciativaVersion.responsable}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">Sponsor ejecutivo</p>
              <p>{data.iniciativaVersion.sponsorEjecutivo}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">1. El problema o la oportunidad</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.descripcionProblema}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">2. Quién lo va a usar y para qué</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.quienUsaYPara}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">3. Valor que generaría</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.impactoEsperado}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">Tiempo estimado para ver ese resultado</p>
              <p>{data.iniciativaVersion.tiempoEstimado || '—'}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">4. Información disponible</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.datosDisponibles}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">¿Esa información está en un sistema accesible?</p>
              <p>{data.iniciativaVersion.informacionAccesible || '—'}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">5. Cómo se hace hoy</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.comoSeHaceHoy}</p>
            </div>
            <div className="md:col-span-3">
              <p className="text-xs text-slate-500">6. Lo que hay que saber antes de avanzar</p>
              <p className="whitespace-pre-wrap">{data.iniciativaVersion.loQueHaySaber}</p>
            </div>
          </CardBody>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Matriz: {data.matriz.nombre}</CardTitle>
          {data.matriz.descripcion && <p className="mt-1 text-sm text-slate-500">{data.matriz.descripcion}</p>}
        </CardHeader>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500 border-b">
              <tr>
                <th className="px-4 py-2 text-left font-medium">Dimensión</th>
                <th className="px-4 py-2 text-right font-medium">Peso</th>
                <th className="px-4 py-2 text-right font-medium">Score</th>
                <th className="px-4 py-2 text-right font-medium">Ponderado</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {data.scores.map((s) => (
                <tr key={s.id}>
                  <td className="px-4 py-2">
                    {s.nombreDimension}
                    {s.dimensionInvertida && <span className="ml-2 text-xs text-slate-500">(invertida)</span>}
                  </td>
                  <td className="px-4 py-2 text-right font-mono">{Number(s.pesoDimension).toFixed(4)}</td>
                  <td className="px-4 py-2 text-right font-semibold">{s.score}</td>
                  <td className="px-4 py-2 text-right font-mono">{Number(s.puntajePonderado).toFixed(4)}</td>
                </tr>
              ))}
              <tr className="bg-slate-50 font-semibold">
                <td colSpan={3} className="px-4 py-2 text-right">Total</td>
                <td className="px-4 py-2 text-right font-mono">{Number(data.puntajeTotal).toFixed(2)}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Vetos</CardTitle>
        </CardHeader>
        <CardBody className="space-y-2">
          {data.vetos.length === 0 ? (
            <p className="text-sm text-slate-500">La matriz no tiene condiciones de veto.</p>
          ) : (
            data.vetos.map((v) => (
              <div
                key={v.id}
                className={`flex items-start gap-2 p-3 rounded-md border ${
                  v.aplica ? 'border-red-300 bg-red-50' : 'border-slate-200 bg-slate-50'
                }`}
              >
                <span
                  className={`shrink-0 w-5 h-5 rounded-full grid place-items-center text-xs font-bold ${
                    v.aplica ? 'bg-red-600 text-white' : 'bg-slate-300 text-slate-700'
                  }`}
                >
                  {v.aplica ? '!' : '·'}
                </span>
                <p className="text-sm">{v.descripcionVeto}</p>
              </div>
            ))
          )}
        </CardBody>
      </Card>
    </div>
  );
}
