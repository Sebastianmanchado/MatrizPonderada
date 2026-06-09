import axios from 'axios';

/**
 * Base URL del API.
 *
 * Producción (Vercel) — dos modos válidos:
 *   A) Proxy: no definir VITE_API_BASE_URL → rutas relativas /api/* (ver vercel.json).
 *   B) Directo: VITE_API_BASE_URL=https://tu-backend.onrender.com → requiere CORS en Render
 *      (FRONTEND_URL = URL exacta de Vercel, sin barra final).
 *
 * Nunca uses localhost en VITE_API_BASE_URL dentro de Vercel.
 */
function resolveBaseUrl() {
  const fromEnv = import.meta.env.VITE_API_BASE_URL?.trim();
  if (import.meta.env.PROD) {
    if (fromEnv && !fromEnv.includes('localhost')) {
      return fromEnv.replace(/\/$/, '');
    }
    return '';
  }
  return fromEnv || 'http://localhost:8080';
}

export const api = axios.create({
  baseURL: resolveBaseUrl(),
  headers: { 'Content-Type': 'application/json' },
});

export const iniciativasApi = {
  list: (params = {}) => api.get('/api/iniciativas', { params }).then((r) => r.data),
  get: (id) => api.get(`/api/iniciativas/${id}`).then((r) => r.data),
  create: (body) => api.post('/api/iniciativas', body).then((r) => r.data),
  remove: (id) => api.delete(`/api/iniciativas/${id}`),
  evaluaciones: (id) => api.get(`/api/iniciativas/${id}/evaluaciones`).then((r) => r.data),
  evaluar: (id, body) => api.post(`/api/iniciativas/${id}/evaluaciones`, body).then((r) => r.data),
  versiones: (id) => api.get(`/api/iniciativas/${id}/versiones`).then((r) => r.data),
  crearVersion: (id, body) => api.post(`/api/iniciativas/${id}/versiones`, body).then((r) => r.data),
};

export const matricesApi = {
  list: () => api.get('/api/matrices').then((r) => r.data),
  get: (id) => api.get(`/api/matrices/${id}`).then((r) => r.data),
  create: (body) => api.post('/api/matrices', body).then((r) => r.data),
  update: (id, body) => api.put(`/api/matrices/${id}`, body).then((r) => r.data),
  desactivar: (id) => api.delete(`/api/matrices/${id}`),
};

export const evaluacionesApi = {
  get: (id) => api.get(`/api/evaluaciones/${id}`).then((r) => r.data),
};
