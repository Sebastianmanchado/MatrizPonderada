// Opciones para los desplegables del formulario de intake (basadas en el
// archivo Excel "1. Intake iniciativas.xlsx" del área de IA).
//
// Si negocio cambia las opciones, se actualiza acá y se ve en NuevaIniciativa,
// NuevaVersionForm y cualquier otra vista que necesite renderizar las opciones.

export const OPCIONES_TIEMPO_ESTIMADO = [
  'Menos de 3 meses',
  '3 a 6 meses',
  '6 a 12 meses',
  'Mas de 12 meses',
  'No lo sé',
];

export const OPCIONES_INFORMACION_ACCESIBLE = [
  'Sí, accesible digitalmente',
  'Existe pero es difícil de acceder',
  'Existe en papel o planillas sueltas',
  'No sé si existe',
  'No existe',
];
