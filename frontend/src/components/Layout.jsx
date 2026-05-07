import { Link, NavLink, Outlet } from 'react-router-dom';
import { cn } from '../lib/cn';

function NavItem({ to, children }) {
  return (
    <NavLink
      to={to}
      end
      className={({ isActive }) =>
        cn(
          'px-3 py-2 rounded-md text-sm font-medium transition',
          isActive
            ? 'bg-brand-50 text-brand-700'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
        )
      }
    >
      {children}
    </NavLink>
  );
}

export function Layout() {
  return (
    <div className="min-h-full flex flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-md bg-brand-600 grid place-items-center text-white font-bold">
              IA
            </div>
            <div>
              <p className="text-sm font-semibold leading-none">Evaluador de Iniciativas IA</p>
              <p className="text-xs text-slate-500 leading-tight">Correo Argentino</p>
            </div>
          </Link>
          <nav className="flex items-center gap-1">
            <NavItem to="/">Inicio</NavItem>
            <NavItem to="/iniciativas">Iniciativas</NavItem>
            <NavItem to="/iniciativas/nueva">Cargar nueva</NavItem>
          </nav>
        </div>
      </header>
      <main className="flex-1">
        <div className="max-w-6xl mx-auto px-6 py-8">
          <Outlet />
        </div>
      </main>
      <footer className="border-t border-slate-200 bg-white">
        <div className="max-w-6xl mx-auto px-6 py-4 text-xs text-slate-500">
          Evaluador IA · Correo Argentino · v0.1
        </div>
      </footer>
    </div>
  );
}
