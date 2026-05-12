import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { iniciativasApi } from '../api/client';
import { Button } from '../components/ui/Button';
import { Input, Select } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { EstadoBadge } from '../components/EstadoBadge';

const ESTADOS = ['SIN_EVALUAR', 'A_REVISAR', 'APROBADO', 'RECHAZADO', 'VETADO'];

function formatFecha(s) {
  if (!s) return '—';
  return new Date(s).toLocaleDateString('es-AR');
}

function formatScore(n) {
  if (n === null || n === undefined) return '—';
  return Number(n).toFixed(2);
}

export function ListadoIniciativas() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const estadoParam = searchParams.get('estado') || '';
  const searchParam = searchParams.get('search') || '';

  const [estado, setEstado] = useState(estadoParam);
  const [search, setSearch] = useState(searchParam);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const params = useMemo(() => {
    const p = {};
    if (estadoParam) p.estado = estadoParam;
    if (searchParam) p.search = searchParam;
    return p;
  }, [estadoParam, searchParam]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    iniciativasApi
      .list(params)
      .then((rows) => {
        if (!cancelled) setData(rows);
      })
      .catch((e) => {
        if (!cancelled) setError(e?.response?.data?.message || 'Error al cargar iniciativas.');
      })
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [params]);

  const aplicarFiltros = (e) => {
    e?.preventDefault();
    const next = {};
    if (estado) next.estado = estado;
    if (search) next.search = search;
    setSearchParams(next);
  };

  const limpiar = () => {
    setEstado('');
    setSearch('');
    setSearchParams({});
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Iniciativas</h1>
        <Button onClick={() => navigate('/iniciativas/nueva')}>+ Nueva</Button>
      </div>

      <Card className="p-4">
        <form onSubmit={aplicarFiltros} className="flex flex-col md:flex-row gap-3 md:items-end">
          <div className="flex-1">
            <label className="block text-xs font-medium text-slate-600 mb-1">Buscar</label>
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Título o responsable…"
            />
          </div>
          <div className="md:w-56">
            <label className="block text-xs font-medium text-slate-600 mb-1">Estado</label>
            <Select value={estado} onChange={(e) => setEstado(e.target.value)}>
              <option value="">Todos</option>
              {ESTADOS.map((e) => (
                <option key={e} value={e}>
                  {e.replace('_', ' ')}
                </option>
              ))}
            </Select>
          </div>
          <div className="flex gap-2">
            <Button type="submit">Aplicar</Button>
            <Button type="button" variant="secondary" onClick={limpiar}>
              Limpiar
            </Button>
          </div>
        </form>
      </Card>

      <Card className="overflow-hidden">
        {loading && <div className="p-6 text-sm text-slate-500">Cargando…</div>}
        {error && <div className="p-6 text-sm text-red-700">{error}</div>}
        {!loading && !error && data.length === 0 && (
          <div className="p-10 text-center text-slate-500">
            <p>No hay iniciativas {estadoParam || searchParam ? 'con estos filtros' : 'cargadas'}.</p>
            <Link className="text-brand-700 underline mt-2 inline-block" to="/iniciativas/nueva">
              Cargar la primera
            </Link>
          </div>
        )}
        {!loading && !error && data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500 border-b">
                <tr>
                  <th className="px-4 py-3 font-medium">Título</th>
                  <th className="px-4 py-3 font-medium">Versión</th>
                  <th className="px-4 py-3 font-medium">Área</th>
                  <th className="px-4 py-3 font-medium">Responsable</th>
                  <th className="px-4 py-3 font-medium">Estado</th>
                  <th className="px-4 py-3 font-medium">Score</th>
                  <th className="px-4 py-3 font-medium">Creada</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((i) => (
                  <tr
                    key={i.id}
                    className="hover:bg-slate-50 cursor-pointer"
                    onClick={() => navigate(`/iniciativas/${i.id}`)}
                  >
                    <td className="px-4 py-3 font-medium text-slate-900">{i.titulo}</td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center rounded-full bg-slate-100 text-slate-700 px-2 py-0.5 text-xs font-semibold">
                        v{i.numeroVersionActual ?? 1}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-700">{i.areaSolicitante}</td>
                    <td className="px-4 py-3 text-slate-700">{i.responsable}</td>
                    <td className="px-4 py-3">
                      <EstadoBadge estado={i.estado} />
                    </td>
                    <td className="px-4 py-3 font-mono">{formatScore(i.scoreMasReciente)}</td>
                    <td className="px-4 py-3 text-slate-600">{formatFecha(i.fechaCreacion)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
