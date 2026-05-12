import { useState } from 'react';
import { iniciativasApi } from '../api/client';
import { Button } from './ui/Button';
import { Input, Textarea, Field } from './ui/Input';

/**
 * Formulario para crear una versión nueva de una iniciativa.
 * Se precarga con el contenido actual ("base") y, al guardar, llama al
 * endpoint POST /api/iniciativas/:id/versiones.
 *
 * Las evaluaciones existentes mantienen su snapshot — solo la siguiente
 * evaluación va a quedar atada a esta nueva versión.
 */
export function NuevaVersionForm({ idIniciativa, base, onCreated, onCancel }) {
  const [titulo, setTitulo] = useState(base.titulo || '');
  const [descripcionProblema, setDescripcionProblema] = useState(base.descripcionProblema || '');
  const [descripcionSolucion, setDescripcionSolucion] = useState(base.descripcionSolucion || '');
  const [areaSolicitante, setAreaSolicitante] = useState(base.areaSolicitante || '');
  const [responsable, setResponsable] = useState(base.responsable || '');
  const [sponsorEjecutivo, setSponsorEjecutivo] = useState(base.sponsorEjecutivo || '');
  const [impactoEsperado, setImpactoEsperado] = useState(base.impactoEsperado || '');
  const [datosDisponibles, setDatosDisponibles] = useState(base.datosDisponibles || '');
  const [usuarioVersion, setUsuarioVersion] = useState('');
  const [comentarioVersion, setComentarioVersion] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const formularioOk =
    titulo.trim().length >= 3 &&
    descripcionProblema.trim().length >= 10 &&
    descripcionSolucion.trim().length >= 10 &&
    areaSolicitante.trim().length >= 2 &&
    responsable.trim().length >= 2 &&
    sponsorEjecutivo.trim().length >= 2 &&
    impactoEsperado.trim().length >= 5 &&
    datosDisponibles.trim().length >= 5 &&
    usuarioVersion.trim().length >= 2;

  const submit = async () => {
    setError(null);
    setSubmitting(true);
    try {
      const body = {
        titulo,
        descripcionProblema,
        descripcionSolucion,
        areaSolicitante,
        responsable,
        sponsorEjecutivo,
        impactoEsperado,
        datosDisponibles,
        usuarioVersion,
        comentarioVersion: comentarioVersion.trim() || null,
      };
      const creada = await iniciativasApi.crearVersion(idIniciativa, body);
      onCreated?.(creada);
    } catch (e) {
      setError(e?.response?.data?.message || 'No se pudo crear la nueva versión.');
    } finally {
      setSubmitting(false);
    }
  };

  const proximoNumero = (base.numeroVersionActual || 1) + 1;

  return (
    <div className="space-y-5">
      <div className="rounded-md bg-amber-50 border border-amber-200 p-3 text-xs text-amber-900">
        Estás por crear la <strong>v{proximoNumero}</strong>. La versión vigente
        (v{base.numeroVersionActual}) queda en el historial y las evaluaciones
        ya hechas siguen apuntando a la versión que evaluaron.
      </div>

      <Field label="Título">
        <Input value={titulo} onChange={(e) => setTitulo(e.target.value)} />
      </Field>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Field label="Área solicitante">
          <Input value={areaSolicitante} onChange={(e) => setAreaSolicitante(e.target.value)} />
        </Field>
        <Field label="Responsable">
          <Input value={responsable} onChange={(e) => setResponsable(e.target.value)} />
        </Field>
        <Field label="Sponsor ejecutivo">
          <Input value={sponsorEjecutivo} onChange={(e) => setSponsorEjecutivo(e.target.value)} />
        </Field>
      </div>

      <Field label="Descripción del problema">
        <Textarea rows={3} value={descripcionProblema} onChange={(e) => setDescripcionProblema(e.target.value)} />
      </Field>
      <Field label="Descripción de la solución">
        <Textarea rows={3} value={descripcionSolucion} onChange={(e) => setDescripcionSolucion(e.target.value)} />
      </Field>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Impacto esperado">
          <Textarea rows={2} value={impactoEsperado} onChange={(e) => setImpactoEsperado(e.target.value)} />
        </Field>
        <Field label="Datos disponibles">
          <Textarea rows={2} value={datosDisponibles} onChange={(e) => setDatosDisponibles(e.target.value)} />
        </Field>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Usuario que crea la versión" hint="Suele ser quien ingresó la iniciativa.">
          <Input value={usuarioVersion} onChange={(e) => setUsuarioVersion(e.target.value)} placeholder="ana.perez" />
        </Field>
        <Field label="Comentario del cambio (opcional)" hint="Por qué se genera esta versión.">
          <Input
            value={comentarioVersion}
            onChange={(e) => setComentarioVersion(e.target.value)}
            placeholder="Sumamos sponsor ejecutivo, ampliamos el dataset…"
          />
        </Field>
      </div>

      {error && <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">{error}</div>}

      <div className="flex justify-end gap-2 border-t pt-4">
        <Button variant="secondary" onClick={onCancel} disabled={submitting}>
          Cancelar
        </Button>
        <Button onClick={submit} disabled={!formularioOk || submitting}>
          {submitting ? 'Creando…' : `Crear v${proximoNumero}`}
        </Button>
      </div>
    </div>
  );
}
