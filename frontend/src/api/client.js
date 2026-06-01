import axios from 'axios';

// En producción dejamos baseURL vacío para que axios use rutas relativas
// (mismo origin que el frontend). Vercel se encarga de hacer rewrite de
// /api/* hacia el backend en Render (ver frontend/vercel.json).
// Eso evita que el navegador del usuario intente conectarse directamente
// a onrender.com, que en algunas redes corporativas está bloqueado.
//
// En desarrollo local apuntamos al backend en localhost:8080, salvo que se
// override con VITE_API_BASE_URL en .env.
const baseURL =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.PROD ? '' : 'http://localhost:8080');

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

export const iniciativasApi = {
  list: (params = {}) => api.get('/api/iniciativas', { params }).then((r) => r.data),
  get: (id) => api.get(`/api/iniciativas/${id}`).then((r) => r.data),
  create: (body) => api.post('/api/iniciativas', body).then((r) => r.data),
  remove: (id) => api.delete(`/api/iniciativas/${id}`),
  evaluaciones: (id) => api.get(`/api/iniciativas/${id}/evaluaciones`).then((r) => r.data),
  evaluar: (id, body) => api.post(`/api/iniciativas/${id}/evaluaciones`, body).then((r) => r.data),
  // Versionado: cada modificación de contenido genera una versión nueva inmutable.
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
