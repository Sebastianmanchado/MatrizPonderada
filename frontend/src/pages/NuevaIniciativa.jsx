import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { iniciativasApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Input, Textarea, Field, Select } from '../components/ui/Input';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';
import {
  OPCIONES_TIEMPO_ESTIMADO,
  OPCIONES_INFORMACION_ACCESIBLE,
} from '../lib/opcionesIniciativa';

// Schema basado en el Excel "1. Intake iniciativas.xlsx".
// Labels, secciones y placeholders provienen directo de ahí.
const schema = z.object({
  titulo: z.string().trim().min(3, 'Mínimo 3 caracteres').max(200),
  areaSolicitante: z.string().trim().min(2, 'Requerido').max(150),
  responsable: z.string().trim().min(2, 'Requerido').max(150),
  sponsorEjecutivo: z.string().trim().min(2, 'Requerido').max(150),
  descripcionProblema: z
    .string()
    .trim()
    .min(10, 'Describí el problema con más detalle')
    .max(2000),
  quienUsaYPara: z
    .string()
    .trim()
    .min(10, 'Describí quién lo va a usar y para qué')
    .max(2000),
  impactoEsperado: z
    .string()
    .trim()
    .min(5, 'Detallá el valor que genera')
    .max(2000),
  tiempoEstimado: z.string().optional().default(''),
  datosDisponibles: z
    .string()
    .trim()
    .min(5, 'Describí la información disponible')
    .max(2000),
  informacionAccesible: z.string().optional().default(''),
  comoSeHaceHoy: z.string().trim().min(5, 'Contá cómo se hace hoy').max(2000),
  loQueHaySaber: z
    .string()
    .trim()
    .min(5, 'Listá lo que hay que saber antes de avanzar')
    .max(2000),
  usuarioCreador: z.string().trim().min(2, 'Requerido').max(150),
});

function fechaHoy() {
  return new Date().toLocaleDateString('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

/**
 * Encabezado de cada sección del intake. Replica el formato del Excel:
 * número + título en mayúsculas con barra a la izquierda.
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

export function NuevaIniciativa() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      titulo: '',
      areaSolicitante: '',
      responsable: '',
      sponsorEjecutivo: '',
      descripcionProblema: '',
      quienUsaYPara: '',
      impactoEsperado: '',
      tiempoEstimado: '',
      datosDisponibles: '',
      informacionAccesible: '',
      comoSeHaceHoy: '',
      loQueHaySaber: '',
      usuarioCreador: '',
    },
  });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      // Limpieza: si el usuario no eligió valor en los selects, los mandamos null
      // para no guardar string vacío en DB.
      const payload = {
        ...values,
        tiempoEstimado: values.tiempoEstimado || null,
        informacionAccesible: values.informacionAccesible || null,
      };
      const creada = await iniciativasApi.create(payload);
      navigate(`/iniciativas/${creada.id}`);
    } catch (e) {
      setServerError(
        e?.response?.data?.message || 'No se pudo crear la iniciativa.',
      );
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardHeader>
          <CardTitle>Formulario de intake — iniciativa de IA</CardTitle>
          <p className="mt-1 text-sm text-slate-500">
            Completá el formulario. Después podés evaluarla con la matriz ponderada.
          </p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* ---------------- IDENTIFICACIÓN ---------------- */}
            <SeccionHeader numero="0" titulo="Identificación" />

            <Field
              label="Nombre de la iniciativa"
              error={errors.titulo?.message}
              hint="Nombre corto y descriptivo. ej. 'Predicción de demanda de envíos – Noviembre'"
            >
              <Input {...register('titulo')} placeholder="Ej. Predicción de demanda de envíos – Noviembre" />
            </Field>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Field
                label="Área solicitante"
                error={errors.areaSolicitante?.message}
                hint="Área que lidera el pedido."
              >
                <Input {...register('areaSolicitante')} placeholder="Atención al cliente" />
              </Field>
              <Field
                label="Nombre y cargo"
                error={errors.responsable?.message}
                hint="Persona que ingresa la iniciativa."
              >
                <Input {...register('responsable')} placeholder="Ana Pérez — Jefa de operaciones" />
              </Field>
              <Field
                label="Sponsor ejecutivo"
                error={errors.sponsorEjecutivo?.message}
                hint="Gerente o director que apoya formalmente esta iniciativa y tiene presupuesto."
              >
                <Input {...register('sponsorEjecutivo')} placeholder="Nombre y apellido" />
              </Field>
              <Field label="Fecha" hint="Se carga sola con la fecha actual.">
                <Input value={fechaHoy()} disabled readOnly />
              </Field>
            </div>

            {/* ---------------- 1. PROBLEMA ---------------- */}
            <SeccionHeader numero="1" titulo="El problema o la oportunidad" />
            <Field
              label="¿Cuál es el problema o la oportunidad que se necesita resolver?"
              error={errors.descripcionProblema?.message}
              hint="Describir como si se explicara a alguien de otra área. Sin tecnicismos. ej. 'Hoy no sabemos qué envíos van a llegar tarde hasta que ya es muy tarde para hacer algo. Queremos anticiparnos.'"
            >
              <Textarea {...register('descripcionProblema')} rows={4} />
            </Field>

            {/* ---------------- 2. USUARIO ---------------- */}
            <SeccionHeader numero="2" titulo="Quién lo va a usar y para qué" />
            <Field
              label="¿Quién es el usuario final del sistema y qué haría con él?"
              error={errors.quienUsaYPara?.message}
              hint="ej. 'El supervisor de distribución. Cada mañana vería una lista de envíos en riesgo y decidiría cuáles priorizar antes del despacho.'"
            >
              <Textarea {...register('quienUsaYPara')} rows={4} />
            </Field>

            {/* ---------------- 3. VALOR ---------------- */}
            <SeccionHeader numero="3" titulo="El valor que generaría" />
            <Field
              label="¿Qué mejora concreta se espera ver si esto funciona?"
              error={errors.impactoEsperado?.message}
              hint="Ponerle números aunque sean estimados. ej. 'Reducir un 10% los envíos que incumplen el SLA' o 'Ahorrar 3 horas diarias de revisión manual en el call center'."
            >
              <Textarea {...register('impactoEsperado')} rows={3} />
            </Field>
            <Field
              label="Tiempo estimado para ver ese resultado"
              error={errors.tiempoEstimado?.message}
              hint="Si es más de 12 meses, puede ser complejo de sostener políticamente."
            >
              <Select {...register('tiempoEstimado')}>
                <option value="">Seleccionar…</option>
                {OPCIONES_TIEMPO_ESTIMADO.map((op) => (
                  <option key={op} value={op}>
                    {op}
                  </option>
                ))}
              </Select>
            </Field>

            {/* ---------------- 4. INFORMACIÓN ---------------- */}
            <SeccionHeader numero="4" titulo="La información disponible" />
            <Field
              label="¿Qué información o registros existen sobre este proceso hoy?"
              error={errors.datosDisponibles?.message}
              hint="No hace falta que sea perfecta ni ordenada. ej. 'Tenemos registros de todos los envíos de los últimos 3 años en el sistema TMS, pero nunca los usamos para analizar patrones.'"
            >
              <Textarea {...register('datosDisponibles')} rows={3} />
            </Field>
            <Field
              label="¿Esa información está en un sistema accesible?"
              error={errors.informacionAccesible?.message}
              hint="No es un filtro excluyente."
            >
              <Select {...register('informacionAccesible')}>
                <option value="">Seleccionar…</option>
                {OPCIONES_INFORMACION_ACCESIBLE.map((op) => (
                  <option key={op} value={op}>
                    {op}
                  </option>
                ))}
              </Select>
            </Field>

            {/* ---------------- 5. CÓMO SE HACE HOY ---------------- */}
            <SeccionHeader numero="5" titulo="Cómo se hace hoy" />
            <Field
              label="¿Cómo se resuelve este problema hoy, sin IA?"
              error={errors.comoSeHaceHoy?.message}
              hint="ej. 'Lo hace una persona manualmente revisando un reporte en Excel todos los días. Tarda unas 2 horas y aun así se escapan casos.'"
            >
              <Textarea {...register('comoSeHaceHoy')} rows={3} />
            </Field>

            {/* ---------------- 6. RIESGOS ---------------- */}
            <SeccionHeader numero="6" titulo="Lo que hay que saber antes de avanzar" />
            <Field
              label="¿Hay alguna restricción, dependencia o riesgo que el equipo de IA debería conocer?"
              error={errors.loQueHaySaber?.message}
              hint="ej. Sistemas que estén por migrar, datos que son confidenciales, áreas que podrían resistir el cambio, fechas límite importantes."
            >
              <Textarea {...register('loQueHaySaber')} rows={3} />
            </Field>

            {/* ---------------- META ---------------- */}
            <div className="border-t pt-4">
              <Field
                label="Usuario creador"
                error={errors.usuarioCreador?.message}
                hint="Tu usuario interno. Sirve para auditoría."
              >
                <Input {...register('usuarioCreador')} placeholder="tu.usuario" />
              </Field>
            </div>

            {serverError && (
              <div className="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-800">
                {serverError}
              </div>
            )}

            <div className="flex justify-end gap-3 pt-2">
              <Button variant="secondary" type="button" onClick={() => navigate(-1)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Guardando…' : 'Guardar iniciativa'}
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>
    </div>
  );
}
