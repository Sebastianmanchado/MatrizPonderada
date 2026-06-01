import { useState } from 'react';
import { iniciativasApi } from '../api/client';
import { Button } from './ui/Button';
import { Input, Textarea, Field, Select } from './ui/Input';
import {
  OPCIONES_TIEMPO_ESTIMADO,
  OPCIONES_INFORMACION_ACCESIBLE,
} from '../lib/opcionesIniciativa';

/**
 * Formulario para crear una versión nueva de una iniciativa.
 * Se precarga con el contenido actual ("base") y, al guardar, llama al
 * endpoint POST /api/iniciativas/:id/versiones.
 *
 * Las evaluaciones existentes mantienen su snapshot — solo la siguiente
 * evaluación va a quedar atada a esta nueva versión.
 *
 * Los labels y placeholders replican los del Excel "Intake iniciativas".
 */
function SeccionHeader({ numero, titulo }) {
  return (
    <div className="border-l-4 border-brand-500 pl-3 py-2 bg-slate-50">
      <p className="text-xs font-semibold tracking-wider text-slate-500 uppercase">
        Sección {numero}
      </p>
      <h3 className="text-base font-bold text-slate-900 uppercase">{titulo}</h3>
    </div>
  );
}

export function NuevaVersionForm({ idIniciativa, base, onCreated, onCancel }) {
  const [titulo, setTitulo] = useState(base.titulo || '');
  const [areaSolicitante, setAreaSolicitante] = useState(base.areaSolicitante || '');
  const [responsable, setResponsable] = useState(base.responsable || '');
  const [sponsorEjecutivo, setSponsorEjecutivo] = useState(base.sponsorEjecutivo || '');
  const [descripcionProblema, setDescripcionProblema] = useState(base.descripcionProblema || '');
  const [quienUsaYPara, setQuienUsaYPara] = useState(base.quienUsaYPara || '');
  const [impactoEsperado, setImpactoEsperado] = useState(base.impactoEsperado || '');
  const [tiempoEstimado, setTiempoEstimado] = useState(base.tiempoEstimado || '');
  const [datosDisponibles, setDatosDisponibles] = useState(base.datosDisponibles || '');
  const [informacionAccesible, setInformacionAccesible] = useState(
    base.informacionAccesible || '',
  );
  const [comoSeHaceHoy, setComoSeHaceHoy] = useState(base.comoSeHaceHoy || '');
  const [loQueHaySaber, setLoQueHaySaber] = useState(base.loQueHaySaber || '');
  const [usuarioVersion, setUsuarioVersion] = useState('');
  const [comentarioVersion, setComentarioVersion] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const formularioOk =
    titulo.trim().length >= 3 &&
    areaSolicitante.trim().length >= 2 &&
    responsable.trim().length >= 2 &&
    sponsorEjecutivo.trim().length >= 2 &&
    descripcionProblema.trim().length >= 10 &&
    quienUsaYPara.trim().length >= 10 &&
    impactoEsperado.trim().length >= 5 &&
    datosDisponibles.trim().length >= 5 &&
    comoSeHaceHoy.trim().length >= 5 &&
    loQueHaySaber.trim().length >= 5 &&
    usuarioVersion.trim().length >= 2;

  const submit = async () => {
    setError(null);
    setSubmitting(true);
    try {
      const body = {
        titulo,
        areaSolicitante,
        responsable,
        sponsorEjecutivo,
        descripcionProblema,
        quienUsaYPara,
        impactoEsperado,
        tiempoEstimado: tiempoEstimado || null,
        datosDisponibles,
        informacionAccesible: informacionAccesible || null,
        comoSeHaceHoy,
        loQueHaySaber,
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
    <div className="space-y-6">
      <div className="rounded-md bg-amber-50 border border-amber-200 p-3 text-xs text-amber-900">
        Estás por crear la <strong>v{proximoNumero}</strong>. La versión vigente
        (v{base.numeroVersionActual}) queda en el historial y las evaluaciones
        ya hechas siguen apuntando a la versión que evaluaron.
      </div>

      <SeccionHeader numero="0" titulo="Identificación" />
      <Field label="Nombre de la iniciativa">
        <Input value={titulo} onChange={(e) => setTitulo(e.target.value)} />
      </Field>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Field label="Área solicitante">
          <Input value={areaSolicitante} onChange={(e) => setAreaSolicitante(e.target.value)} />
        </Field>
        <Field label="Nombre y cargo">
          <Input value={responsable} onChange={(e) => setResponsable(e.target.value)} />
        </Field>
        <Field label="Sponsor ejecutivo">
          <Input value={sponsorEjecutivo} onChange={(e) => setSponsorEjecutivo(e.target.value)} />
        </Field>
      </div>

      <SeccionHeader numero="1" titulo="El problema o la oportunidad" />
      <Field
        label="¿Cuál es el problema o la oportunidad que se necesita resolver?"
        hint="Describir como si se explicara a alguien de otra área. Sin tecnicismos."
      >
        <Textarea
          rows={3}
          value={descripcionProblema}
          onChange={(e) => setDescripcionProblema(e.target.value)}
        />
      </Field>

      <SeccionHeader numero="2" titulo="Quién lo va a usar y para qué" />
      <Field
        label="¿Quién es el usuario final del sistema y qué haría con él?"
        hint="Indicá rol/perfil y la acción concreta que tomaría."
      >
        <Textarea rows={3} value={quienUsaYPara} onChange={(e) => setQuienUsaYPara(e.target.value)} />
      </Field>

      <SeccionHeader numero="3" titulo="El valor que generaría" />
      <Field
        label="¿Qué mejora concreta se espera ver si esto funciona?"
        hint="Ponerle números aunque sean estimados."
      >
        <Textarea
          rows={2}
          value={impactoEsperado}
          onChange={(e) => setImpactoEsperado(e.target.value)}
        />
      </Field>
      <Field
        label="Tiempo estimado para ver ese resultado"
        hint="Si es más de 12 meses, puede ser complejo de sostener políticamente."
      >
        <Select value={tiempoEstimado} onChange={(e) => setTiempoEstimado(e.target.value)}>
          <option value="">Seleccionar…</option>
          {OPCIONES_TIEMPO_ESTIMADO.map((op) => (
            <option key={op} value={op}>
              {op}
            </option>
          ))}
        </Select>
      </Field>

      <SeccionHeader numero="4" titulo="La información disponible" />
      <Field
        label="¿Qué información o registros existen sobre este proceso hoy?"
        hint="No hace falta que sea perfecta ni ordenada."
      >
        <Textarea
          rows={2}
          value={datosDisponibles}
          onChange={(e) => setDatosDisponibles(e.target.value)}
        />
      </Field>
      <Field
        label="¿Esa información está en un sistema accesible?"
        hint="No es un filtro excluyente."
      >
        <Select
          value={informacionAccesible}
          onChange={(e) => setInformacionAccesible(e.target.value)}
        >
          <option value="">Seleccionar…</option>
          {OPCIONES_INFORMACION_ACCESIBLE.map((op) => (
            <option key={op} value={op}>
              {op}
            </option>
          ))}
        </Select>
      </Field>

      <SeccionHeader numero="5" titulo="Cómo se hace hoy" />
      <Field label="¿Cómo se resuelve este problema hoy, sin IA?">
        <Textarea rows={2} value={comoSeHaceHoy} onChange={(e) => setComoSeHaceHoy(e.target.value)} />
      </Field>

      <SeccionHeader numero="6" titulo="Lo que hay que saber antes de avanzar" />
      <Field label="¿Hay alguna restricción, dependencia o riesgo que el equipo de IA debería conocer?">
        <Textarea rows={2} value={loQueHaySaber} onChange={(e) => setLoQueHaySaber(e.target.value)} />
      </Field>

      <div className="border-t pt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Usuario que crea la versión" hint="Suele ser quien ingresó la iniciativa.">
          <Input
            value={usuarioVersion}
            onChange={(e) => setUsuarioVersion(e.target.value)}
            placeholder="ana.perez"
          />
        </Field>
        <Field label="Comentario del cambio (opcional)" hint="Por qué se genera esta versión.">
          <Input
            value={comentarioVersion}
            onChange={(e) => setComentarioVersion(e.target.value)}
            placeholder="Sumamos sponsor ejecutivo, ampliamos el dataset…"
          />
        </Field>
      </div>

      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">
          {error}
        </div>
      )}

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
