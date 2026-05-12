import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { iniciativasApi, matricesApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Input, Textarea, Field, Select } from '../components/ui/Input';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';
import { ArquetipoBadge, ResultadoBadge } from '../components/EstadoBadge';
import { MatrizForm } from '../components/MatrizForm';

const UMBRAL_AVANZA = 3.5;
const UMBRAL_REVISAR = 2.5;
const UMBRAL_ESFUERZO_QUICK = 3;

function calcularPreview(matriz, scores, vetos) {
  if (!matriz) return { puntaje: 0, resultado: null, arquetipo: null, tieneVeto: false };

  const tieneVeto = matriz.vetos.some((v) => vetos[v.id] === true);

  let puntaje = 0;
  for (const d of matriz.dimensiones) {
    const s = scores[d.id];
    if (s) puntaje += s * Number(d.peso);
  }
  puntaje = Math.round(puntaje * 100) / 100;

  let resultado;
  if (tieneVeto) resultado = 'DESCARTAR';
  else if (puntaje >= UMBRAL_AVANZA) resultado = 'AVANZA';
  else if (puntaje >= UMBRAL_REVISAR) resultado = 'REVISAR';
  else resultado = 'DESCARTAR';

  let dimEsfuerzo = matriz.dimensiones.find((d) => d.invertida);
  if (!dimEsfuerzo) {
    dimEsfuerzo = matriz.dimensiones.find((d) => (d.nombre || '').toLowerCase().includes('esfuerzo'));
  }
  const scoreEsfuerzo = dimEsfuerzo ? scores[dimEsfuerzo.id] : null;

  let arquetipo;
  if (tieneVeto) arquetipo = 'A_REVISAR';
  else if (puntaje < UMBRAL_REVISAR) arquetipo = 'TIME_WASTER';
  else if (puntaje >= UMBRAL_AVANZA) {
    if (!scoreEsfuerzo) arquetipo = 'A_REVISAR';
    else arquetipo = scoreEsfuerzo >= UMBRAL_ESFUERZO_QUICK ? 'QUICK_WIN' : 'MAJOR_PROJECT';
  } else {
    arquetipo = 'A_REVISAR';
  }

  return { puntaje, resultado, arquetipo, tieneVeto };
}

function ScoreSelector({ value, onChange }) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          className={`w-9 h-9 rounded-md border text-sm font-semibold transition ${
            value === n
              ? 'bg-brand-600 border-brand-600 text-white'
              : 'bg-white border-slate-300 text-slate-700 hover:bg-slate-50'
          }`}
        >
          {n}
        </button>
      ))}
    </div>
  );
}

export function Evaluar() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [iniciativa, setIniciativa] = useState(null);
  const [matrices, setMatrices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);

  const [matrizSel, setMatrizSel] = useState(null);
  const [mostrandoNuevaMatriz, setMostrandoNuevaMatriz] = useState(false);

  const [scores, setScores] = useState({});
  const [vetos, setVetos] = useState({});
  const [notas, setNotas] = useState('');
  const [usuarioEvaluador, setUsuarioEvaluador] = useState('');

  const [submitting, setSubmitting] = useState(false);
  const [errorGuardar, setErrorGuardar] = useState(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([iniciativasApi.get(id), matricesApi.list()])
      .then(([ini, mats]) => {
        if (cancelled) return;
        setIniciativa(ini);
        setMatrices(mats);
        if (mats.length > 0) setMatrizSel(mats[0]);
      })
      .catch((e) => !cancelled && setErrorCarga(e?.response?.data?.message || 'Error al cargar.'))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [id]);

  // reset scores y vetos al cambiar de matriz
  useEffect(() => {
    if (!matrizSel) {
      setScores({});
      setVetos({});
      return;
    }
    const initScores = {};
    const initVetos = {};
    matrizSel.dimensiones.forEach((d) => { initScores[d.id] = null; });
    matrizSel.vetos.forEach((v) => { initVetos[v.id] = false; });
    setScores(initScores);
    setVetos(initVetos);
  }, [matrizSel]);

  const preview = useMemo(() => calcularPreview(matrizSel, scores, vetos), [matrizSel, scores, vetos]);

  const todosScoresCargados = matrizSel
    ? matrizSel.dimensiones.every((d) => scores[d.id] >= 1 && scores[d.id] <= 5)
    : false;

  const puedeGuardar = matrizSel && todosScoresCargados && usuarioEvaluador.trim().length >= 2;

  const onMatrizCreada = (creada) => {
    setMatrices((prev) => [creada, ...prev]);
    setMatrizSel(creada);
    setMostrandoNuevaMatriz(false);
  };

  const onCambioSelect = (e) => {
    const v = e.target.value;
    if (v === '__crear__') {
      setMostrandoNuevaMatriz(true);
      return;
    }
    const m = matrices.find((x) => String(x.id) === v);
    if (m) setMatrizSel(m);
  };

  const guardar = async () => {
    setErrorGuardar(null);
    setSubmitting(true);
    try {
      const body = {
        idMatriz: matrizSel.id,
        usuarioEvaluador,
        scores: matrizSel.dimensiones.map((d) => ({ idMatrizDimension: d.id, score: scores[d.id] })),
        vetos: matrizSel.vetos.map((v) => ({ idMatrizVeto: v.id, aplica: !!vetos[v.id] })),
        notas: notas || null,
      };
      await iniciativasApi.evaluar(id, body);
      navigate(`/iniciativas/${id}`);
    } catch (e) {
      setErrorGuardar(e?.response?.data?.message || 'No se pudo guardar la evaluación.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-6 text-sm text-slate-500">Cargando…</div>;
  if (errorCarga) return <div className="p-6 text-sm text-red-700">{errorCarga}</div>;
  if (!iniciativa) return null;

  return (
    <div className="space-y-6">
      <div>
        <Link to={`/iniciativas/${id}`} className="text-sm text-slate-600 hover:underline">
          ← Volver a la iniciativa
        </Link>
        <div className="mt-2 flex items-center gap-3 flex-wrap">
          <h1 className="text-2xl font-bold">Evaluar: {iniciativa.titulo}</h1>
          {iniciativa.numeroVersionActual != null && (
            <span className="inline-flex items-center rounded-full bg-brand-100 text-brand-800 px-2 py-0.5 text-xs font-semibold">
              v{iniciativa.numeroVersionActual} <span className="ml-1 text-[10px] uppercase">actual</span>
            </span>
          )}
        </div>
        <p className="mt-1 text-sm text-slate-500">
          {iniciativa.areaSolicitante} · {iniciativa.responsable}
        </p>
        <p className="mt-1 text-xs text-slate-500">
          La evaluación queda atada a la versión <strong>v{iniciativa.numeroVersionActual}</strong>. Si después se crea
          una versión nueva, esta evaluación sigue mostrando este snapshot.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Matriz de evaluación</CardTitle>
            </CardHeader>
            <CardBody className="space-y-4">
              {!mostrandoNuevaMatriz && (
                <Field label="Seleccionar matriz">
                  <Select value={matrizSel ? String(matrizSel.id) : ''} onChange={onCambioSelect}>
                    {matrices.length === 0 && <option value="">No hay matrices activas</option>}
                    {matrices.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.nombre} ({m.dimensiones.length} dimensiones)
                      </option>
                    ))}
                    <option value="__crear__">+ Crear nueva matriz…</option>
                  </Select>
                </Field>
              )}

              {mostrandoNuevaMatriz && (
                <div className="rounded-md border border-brand-200 bg-brand-50/30 p-4">
                  <h3 className="font-semibold mb-3">Crear nueva matriz</h3>
                  <MatrizForm
                    onCreated={onMatrizCreada}
                    onCancel={() => setMostrandoNuevaMatriz(false)}
                  />
                </div>
              )}
            </CardBody>
          </Card>

          {matrizSel && !mostrandoNuevaMatriz && (
            <Card>
              <CardHeader>
                <CardTitle>Puntuación por dimensión</CardTitle>
                <p className="mt-1 text-xs text-slate-500">Escala 1 (peor) a 5 (mejor). Para dimensiones invertidas, 5 = mínima complejidad / menor riesgo.</p>
              </CardHeader>
              <CardBody className="space-y-5">
                {matrizSel.dimensiones.map((d) => (
                  <div key={d.id} className="rounded-md border border-slate-200 p-3">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="font-medium">
                          {d.nombre}
                          {d.invertida && (
                            <span className="ml-2 text-[11px] uppercase tracking-wide text-amber-700 bg-amber-100 border border-amber-200 px-1.5 py-0.5 rounded">
                              invertida — 5 = mín. complejidad
                            </span>
                          )}
                        </p>
                        {d.descripcion && <p className="mt-1 text-xs text-slate-600">{d.descripcion}</p>}
                      </div>
                      <span className="shrink-0 text-xs text-slate-500 font-mono">peso {Number(d.peso).toFixed(4)}</span>
                    </div>
                    <div className="mt-3">
                      <ScoreSelector
                        value={scores[d.id]}
                        onChange={(n) => setScores((prev) => ({ ...prev, [d.id]: n }))}
                      />
                    </div>
                  </div>
                ))}
              </CardBody>
            </Card>
          )}

          {matrizSel && !mostrandoNuevaMatriz && matrizSel.vetos.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Condiciones de veto</CardTitle>
                <p className="mt-1 text-xs text-slate-500">Activá las que aplican. Si alguna está activa, el resultado será DESCARTAR.</p>
              </CardHeader>
              <CardBody className="space-y-2">
                {matrizSel.vetos.map((v) => {
                  const activo = !!vetos[v.id];
                  return (
                    <label
                      key={v.id}
                      className={`flex items-start gap-3 p-3 rounded-md border cursor-pointer transition ${
                        activo
                          ? 'border-red-300 bg-red-50'
                          : 'border-slate-200 bg-white hover:bg-slate-50'
                      }`}
                    >
                      <input
                        type="checkbox"
                        className="mt-0.5 h-4 w-4"
                        checked={activo}
                        onChange={(e) => setVetos((prev) => ({ ...prev, [v.id]: e.target.checked }))}
                      />
                      <span className="text-sm">{v.descripcion}</span>
                    </label>
                  );
                })}
              </CardBody>
            </Card>
          )}

          {matrizSel && !mostrandoNuevaMatriz && (
            <Card>
              <CardHeader>
                <CardTitle>Datos de la evaluación</CardTitle>
              </CardHeader>
              <CardBody className="space-y-4">
                <Field label="Evaluador">
                  <Input value={usuarioEvaluador} onChange={(e) => setUsuarioEvaluador(e.target.value)} placeholder="tu.usuario" />
                </Field>
                <Field label="Notas (opcional)">
                  <Textarea rows={3} value={notas} onChange={(e) => setNotas(e.target.value)} />
                </Field>
              </CardBody>
            </Card>
          )}
        </div>

        <div className="space-y-4 lg:sticky lg:top-6 self-start">
          <Card>
            <CardHeader>
              <CardTitle>Resultado en vivo</CardTitle>
            </CardHeader>
            <CardBody className="space-y-4">
              {!matrizSel || mostrandoNuevaMatriz ? (
                <p className="text-sm text-slate-500">Seleccioná una matriz para empezar.</p>
              ) : !todosScoresCargados ? (
                <p className="text-sm text-slate-500">Completá los scores para ver el cálculo.</p>
              ) : (
                <>
                  <div>
                    <p className="text-xs text-slate-500">Puntaje ponderado</p>
                    <p className="font-mono text-3xl font-bold">{preview.puntaje.toFixed(2)}</p>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <p className="text-xs text-slate-500">Arquetipo</p>
                      <div className="mt-1"><ArquetipoBadge arquetipo={preview.arquetipo} /></div>
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Resultado</p>
                      <div className="mt-1"><ResultadoBadge resultado={preview.resultado} /></div>
                    </div>
                  </div>
                  {preview.tieneVeto && (
                    <div className="rounded-md bg-slate-900 text-white p-3 text-sm font-semibold text-center">
                      VETADA → DESCARTAR
                    </div>
                  )}
                </>
              )}
            </CardBody>
          </Card>

          {errorGuardar && (
            <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">
              {errorGuardar}
            </div>
          )}

          <Button
            className="w-full"
            size="lg"
            onClick={guardar}
            disabled={!puedeGuardar || submitting}
          >
            {submitting ? 'Guardando…' : 'Guardar evaluación'}
          </Button>
          {!puedeGuardar && matrizSel && (
            <p className="text-xs text-slate-500 text-center">
              Completá todos los scores y el evaluador para habilitar el guardado.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
