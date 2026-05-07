import { cn } from '../lib/cn';

const estilos = {
  SIN_EVALUAR: 'bg-slate-100 text-slate-700 border-slate-200',
  RECHAZADO:   'bg-red-100 text-red-800 border-red-200',
  A_REVISAR:   'bg-amber-100 text-amber-800 border-amber-200',
  APROBADO:    'bg-emerald-100 text-emerald-800 border-emerald-200',
  VETADO:      'bg-slate-900 text-white border-slate-900',
};

const labels = {
  SIN_EVALUAR: 'Sin evaluar',
  RECHAZADO: 'Rechazado',
  A_REVISAR: 'A revisar',
  APROBADO: 'Aprobado',
  VETADO: 'Vetado',
};

export function EstadoBadge({ estado, className }) {
  if (!estado) return null;
  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded-full border text-xs font-medium',
        estilos[estado] || estilos.SIN_EVALUAR,
        className
      )}
    >
      {labels[estado] || estado}
    </span>
  );
}

const arquetipoLabels = {
  QUICK_WIN: 'Quick Win',
  MAJOR_PROJECT: 'Major Project',
  TIME_WASTER: 'Time Waster',
  A_REVISAR: 'A revisar',
};

const arquetipoEstilos = {
  QUICK_WIN: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  MAJOR_PROJECT: 'bg-blue-50 text-blue-700 border-blue-200',
  TIME_WASTER: 'bg-red-50 text-red-700 border-red-200',
  A_REVISAR: 'bg-amber-50 text-amber-700 border-amber-200',
};

export function ArquetipoBadge({ arquetipo, className }) {
  if (!arquetipo) return null;
  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded-full border text-xs font-medium',
        arquetipoEstilos[arquetipo] || arquetipoEstilos.A_REVISAR,
        className
      )}
    >
      {arquetipoLabels[arquetipo] || arquetipo}
    </span>
  );
}

const resultadoLabels = {
  AVANZA: 'Avanza',
  REVISAR: 'Revisar',
  DESCARTAR: 'Descartar',
};

const resultadoEstilos = {
  AVANZA: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  REVISAR: 'bg-amber-100 text-amber-800 border-amber-200',
  DESCARTAR: 'bg-red-100 text-red-800 border-red-200',
};

export function ResultadoBadge({ resultado, className }) {
  if (!resultado) return null;
  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded-full border text-xs font-semibold',
        resultadoEstilos[resultado] || '',
        className
      )}
    >
      {resultadoLabels[resultado] || resultado}
    </span>
  );
}
