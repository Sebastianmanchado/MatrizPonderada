import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { iniciativasApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Input, Textarea, Field } from '../components/ui/Input';
import { Card, CardBody, CardHeader, CardTitle } from '../components/ui/Card';

const schema = z.object({
  titulo: z.string().trim().min(3, 'Mínimo 3 caracteres').max(200),
  areaSolicitante: z.string().trim().min(2, 'Requerido').max(150),
  responsable: z.string().trim().min(2, 'Requerido').max(150),
  sponsorEjecutivo: z.string().trim().min(2, 'Requerido').max(150),
  descripcionProblema: z.string().trim().min(10, 'Describí el problema con más detalle').max(2000),
  descripcionSolucion: z.string().trim().min(10, 'Describí la solución propuesta').max(2000),
  impactoEsperado: z.string().trim().min(5, 'Detallá el impacto esperado').max(2000),
  datosDisponibles: z.string().trim().min(5, 'Describí los datos disponibles').max(2000),
  usuarioCreador: z.string().trim().min(2, 'Requerido').max(150),
});

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
      descripcionSolucion: '',
      impactoEsperado: '',
      datosDisponibles: '',
      usuarioCreador: '',
    },
  });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      const creada = await iniciativasApi.create(values);
      navigate(`/iniciativas/${creada.id}`);
    } catch (e) {
      setServerError(e?.response?.data?.message || 'No se pudo crear la iniciativa.');
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardHeader>
          <CardTitle>Nueva iniciativa</CardTitle>
          <p className="mt-1 text-sm text-slate-500">
            Completá el intake. Después podés evaluarla con la matriz ponderada.
          </p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Field label="Título" error={errors.titulo?.message}>
              <Input {...register('titulo')} placeholder="Asistente de clasificación de reclamos" />
            </Field>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Field label="Área solicitante" error={errors.areaSolicitante?.message}>
                <Input {...register('areaSolicitante')} placeholder="Atención al cliente" />
              </Field>
              <Field label="Responsable" error={errors.responsable?.message}>
                <Input {...register('responsable')} placeholder="Nombre y apellido" />
              </Field>
              <Field label="Sponsor ejecutivo" error={errors.sponsorEjecutivo?.message}>
                <Input {...register('sponsorEjecutivo')} placeholder="Nombre y apellido" />
              </Field>
            </div>

            <Field label="Descripción del problema" error={errors.descripcionProblema?.message}>
              <Textarea {...register('descripcionProblema')} rows={4} />
            </Field>

            <Field label="Descripción de la solución" error={errors.descripcionSolucion?.message}>
              <Textarea {...register('descripcionSolucion')} rows={4} />
            </Field>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Field label="Impacto esperado" error={errors.impactoEsperado?.message}>
                <Textarea {...register('impactoEsperado')} rows={3} />
              </Field>
              <Field label="Datos disponibles" error={errors.datosDisponibles?.message}>
                <Textarea {...register('datosDisponibles')} rows={3} />
              </Field>
            </div>

            <Field label="Usuario creador" error={errors.usuarioCreador?.message}>
              <Input {...register('usuarioCreador')} placeholder="tu.usuario" />
            </Field>

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
