import { useMemo, useState } from 'react';
import { matricesApi } from '../api/client';
import { Button } from './ui/Button';
import { Input, Textarea, Field, Select } from './ui/Input';

const TOLERANCIA = 0.0001;

const dimVacia = (orden) => ({ nombre: '', descripcion: '', peso: '', orden, invertida: false });
const vetoVacio = (orden) => ({ descripcion: '', orden });

export function MatrizForm({ onCreated, onCancel }) {
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [usuarioCreador, setUsuarioCreador] = useState('');
  const [dimensiones, setDimensiones] = useState([dimVacia(1), dimVacia(2)]);
  const [vetos, setVetos] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const sumaPesos = useMemo(() => {
    return dimensiones.reduce((acc, d) => acc + (parseFloat(d.peso) || 0), 0);
  }, [dimensiones]);

  const sumaOk = Math.abs(sumaPesos - 1) <= TOLERANCIA;

  const addDimension = () => setDimensiones((arr) => [...arr, dimVacia(arr.length + 1)]);
  const removeDimension = (idx) =>
    setDimensiones((arr) => arr.filter((_, i) => i !== idx).map((d, i) => ({ ...d, orden: i + 1 })));
  const updateDim = (idx, patch) =>
    setDimensiones((arr) => arr.map((d, i) => (i === idx ? { ...d, ...patch } : d)));

  const addVeto = () => setVetos((arr) => [...arr, vetoVacio(arr.length + 1)]);
  const removeVeto = (idx) =>
    setVetos((arr) => arr.filter((_, i) => i !== idx).map((v, i) => ({ ...v, orden: i + 1 })));
  const updateVeto = (idx, patch) =>
    setVetos((arr) => arr.map((v, i) => (i === idx ? { ...v, ...patch } : v)));

  const formularioOk =
    nombre.trim().length >= 3 &&
    usuarioCreador.trim().length >= 2 &&
    dimensiones.length >= 1 &&
    dimensiones.every((d) => d.nombre.trim().length >= 2 && parseFloat(d.peso) >= 0) &&
    vetos.every((v) => v.descripcion.trim().length >= 5) &&
    sumaOk;

  const submit = async () => {
    setError(null);
    setSubmitting(true);
    try {
      const body = {
        nombre,
        descripcion,
        usuarioCreador,
        dimensiones: dimensiones.map((d) => ({
          nombre: d.nombre,
          descripcion: d.descripcion || null,
          peso: parseFloat(d.peso),
          orden: d.orden,
          invertida: !!d.invertida,
        })),
        vetos: vetos.map((v) => ({ descripcion: v.descripcion, orden: v.orden })),
      };
      const creada = await matricesApi.create(body);
      onCreated?.(creada);
    } catch (e) {
      setError(e?.response?.data?.message || 'No se pudo crear la matriz.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Nombre">
          <Input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Ej. Matriz interna 2026 v2" />
        </Field>
        <Field label="Usuario creador">
          <Input value={usuarioCreador} onChange={(e) => setUsuarioCreador(e.target.value)} placeholder="tu.usuario" />
        </Field>
      </div>
      <Field label="Descripción">
        <Textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} rows={2} />
      </Field>

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h4 className="font-semibold text-slate-900">Dimensiones ({dimensiones.length})</h4>
          <Button size="sm" variant="secondary" onClick={addDimension}>+ Agregar dimensión</Button>
        </div>
        <div className="space-y-3">
          {dimensiones.map((d, i) => (
            <div key={i} className="rounded-md border border-slate-200 p-3 bg-slate-50/50">
              <div className="grid grid-cols-12 gap-3">
                <Field label="Nombre" className="col-span-12 md:col-span-4">
                  <Input value={d.nombre} onChange={(e) => updateDim(i, { nombre: e.target.value })} />
                </Field>
                <Field label="Peso (0–1)" className="col-span-6 md:col-span-2">
                  <Input
                    type="number"
                    step="0.01"
                    min="0"
                    max="1"
                    value={d.peso}
                    onChange={(e) => updateDim(i, { peso: e.target.value })}
                  />
                </Field>
                <Field label="Orden" className="col-span-3 md:col-span-1">
                  <Input type="number" value={d.orden} onChange={(e) => updateDim(i, { orden: parseInt(e.target.value, 10) || 0 })} />
                </Field>
                <Field label="Invertida" className="col-span-3 md:col-span-2">
                  <label className="flex items-center gap-2 h-10">
                    <input
                      type="checkbox"
                      checked={!!d.invertida}
                      onChange={(e) => updateDim(i, { invertida: e.target.checked })}
                      className="h-4 w-4"
                    />
                    <span className="text-xs text-slate-600">5 = mín. complejidad</span>
                  </label>
                </Field>
                <div className="col-span-12 md:col-span-3 flex md:items-end justify-end">
                  <Button size="sm" variant="ghost" onClick={() => removeDimension(i)} disabled={dimensiones.length === 1}>
                    Quitar
                  </Button>
                </div>
                <Field label="Descripción" className="col-span-12">
                  <Textarea rows={2} value={d.descripcion} onChange={(e) => updateDim(i, { descripcion: e.target.value })} />
                </Field>
              </div>
            </div>
          ))}
        </div>
        <div
          className={`rounded-md border px-3 py-2 text-sm font-medium ${
            sumaOk ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
          }`}
        >
          Suma de pesos: <span className="font-mono">{sumaPesos.toFixed(4)}</span>{' '}
          {sumaOk ? '✓ correcta' : '✗ debe ser exactamente 1.0000'}
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h4 className="font-semibold text-slate-900">Vetos ({vetos.length})</h4>
          <Button size="sm" variant="secondary" onClick={addVeto}>+ Agregar veto</Button>
        </div>
        {vetos.length === 0 && <p className="text-xs text-slate-500">La matriz puede no tener vetos.</p>}
        <div className="space-y-2">
          {vetos.map((v, i) => (
            <div key={i} className="flex items-start gap-2">
              <span className="mt-2 text-sm text-slate-500 w-8 text-right">#{v.orden}</span>
              <div className="flex-1">
                <Textarea
                  rows={2}
                  value={v.descripcion}
                  onChange={(e) => updateVeto(i, { descripcion: e.target.value })}
                  placeholder="Ej: No hay sponsor ejecutivo identificado"
                />
              </div>
              <Button size="sm" variant="ghost" onClick={() => removeVeto(i)}>Quitar</Button>
            </div>
          ))}
        </div>
      </div>

      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">{error}</div>
      )}

      <div className="flex justify-end gap-2 border-t pt-4">
        <Button variant="secondary" onClick={onCancel} disabled={submitting}>Cancelar</Button>
        <Button onClick={submit} disabled={!formularioOk || submitting}>
          {submitting ? 'Creando…' : 'Crear matriz'}
        </Button>
      </div>
    </div>
  );
}
