import { Link } from 'react-router-dom';

export function Home() {
  return (
    <div className="space-y-8">
      <div className="text-center max-w-2xl mx-auto pt-8">
        <h1 className="text-3xl font-bold text-slate-900">
          Evaluador de iniciativas de IA
        </h1>
        <p className="mt-3 text-slate-600">
          Cargá una iniciativa, evaluala con la matriz ponderada y obtené automáticamente
          el puntaje, arquetipo (Quick Win, Major Project, Time Waster) y resultado
          (Avanza, Revisar, Descartar).
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
        <Link
          to="/iniciativas/nueva"
          className="group rounded-xl border-2 border-brand-200 bg-white hover:border-brand-500 hover:shadow-md transition p-8 text-center"
        >
          <div className="w-12 h-12 mx-auto rounded-full bg-brand-100 grid place-items-center text-brand-700 text-xl font-bold group-hover:bg-brand-600 group-hover:text-white transition">
            +
          </div>
          <h2 className="mt-4 text-lg font-semibold">Cargar iniciativa</h2>
          <p className="mt-1 text-sm text-slate-600">
            Crear el intake de una nueva iniciativa.
          </p>
        </Link>

        <Link
          to="/iniciativas"
          className="group rounded-xl border-2 border-slate-200 bg-white hover:border-slate-400 hover:shadow-md transition p-8 text-center"
        >
          <div className="w-12 h-12 mx-auto rounded-full bg-slate-100 grid place-items-center text-slate-700 text-xl font-bold group-hover:bg-slate-700 group-hover:text-white transition">
            =
          </div>
          <h2 className="mt-4 text-lg font-semibold">Revisar iniciativas</h2>
          <p className="mt-1 text-sm text-slate-600">
            Ver el listado, filtrar por estado y abrir el detalle.
          </p>
        </Link>
      </div>
    </div>
  );
}
