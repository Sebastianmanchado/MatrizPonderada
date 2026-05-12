# Evaluador de Iniciativas IA — Correo Argentino

Aplicación full-stack para evaluar iniciativas de IA mediante matrices de decisión ponderada.

## Stack

- **Backend**: Spring Boot 3.3 · Spring Data JPA · Java 17 · Maven
- **Base de datos**: PostgreSQL 16 (local en Docker · Neon en producción)
- **Frontend**: React 18 · Vite · React Router · Axios · Tailwind CSS
- **Validación**: Bean Validation (backend) · react-hook-form + zod (frontend)
- **Deploy**: backend en Render (Docker) · DB en Neon

## Requisitos previos

- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -v`)
- Node 18+ (`node -v`)
- Docker Desktop con compose v2 (`docker compose version`)

## Variables de entorno

El repo trae `.env.example` en la raíz. Copialo a `.env`:

```bash
cp .env.example .env
```

El `.env` está gitignoreado. Definí ahí los valores reales en local (en Render se setean desde el panel de Environment).

## Levantar la base de datos local (PostgreSQL en Docker)

Desde la raíz del repo:

```bash
docker compose up -d
```

Esto levanta PostgreSQL 16 (`postgres:16-alpine`) en `localhost:5432` con un volumen nombrado para persistir datos entre reinicios. Usuario, password y nombre de base se leen del `.env`. Por defecto:

- Usuario: `matriz`
- Password: `matriz_dev_password`
- Base: `matriz_ponderada`

Para parar la base (los datos se conservan en el volumen):

```bash
docker compose down
```

Para parar y **borrar todos los datos** (vuelve a un estado limpio):

```bash
docker compose down -v
```

Ver logs:

```bash
docker compose logs -f postgres
```

Conectarte con `psql` sin instalar nada en el host:

```bash
docker exec -it matriz-postgres psql -U matriz -d matriz_ponderada
```

## Backend

Con la base ya corriendo y el `.env` configurado:

```bash
cd backend
mvn spring-boot:run
```

La app escucha en `http://localhost:8080`. Hibernate genera el schema automáticamente al primer arranque (`ddl-auto=update`). En el primer arranque, un `DataSeeder` inserta la matriz inicial "Evaluador Correo Argentino - 6 dimensiones" con sus dimensiones y vetos.

Tests:

```bash
cd backend
mvn test
```

## Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

La UI escucha en `http://localhost:5173` y llama al backend en `http://localhost:8080`.

## Endpoints REST

| Método | Path                                   | Descripción                                                              |
| ------ | -------------------------------------- | ------------------------------------------------------------------------ |
| POST   | /api/iniciativas                       | Crear iniciativa                                                         |
| GET    | /api/iniciativas                       | Listar (filtros: `?estado=...&search=...`)                               |
| GET    | /api/iniciativas/{id}                  | Detalle + evaluaciones (resumen)                                         |
| PUT    | /api/iniciativas/{id}                  | Editar campos del intake                                                 |
| DELETE | /api/iniciativas/{id}                  | Hard-delete (borra evaluaciones asociadas en cascada)                    |
| GET    | /api/matrices                          | Listar matrices con `activa=true`                                        |
| GET    | /api/matrices/{id}                     | Detalle (dimensiones + vetos)                                            |
| POST   | /api/matrices                          | Crear matriz nueva                                                       |
| PUT    | /api/matrices/{id}                     | Editar — solo si NO tiene evaluaciones asociadas (409 si las tiene)      |
| DELETE | /api/matrices/{id}                     | Soft-delete (`activa=false`). NO permite hard-delete                     |
| POST   | /api/iniciativas/{id}/evaluaciones     | Crear evaluación (calcula score, arquetipo, resultado y actualiza estado)|
| GET    | /api/iniciativas/{id}/evaluaciones     | Historial DESC por fecha                                                 |
| GET    | /api/evaluaciones/{id}                 | Detalle: scores + vetos aplicados + matriz usada                         |

Hay un archivo [`backend/api.http`](backend/api.http) con requests de prueba listos para usar con la extensión REST Client de VS Code o IntelliJ HTTP Client.

## Decisiones de implementación

- **Paquete Java**: `com.correoargentino.evaluador`.
- **UI**: se usa Tailwind + componentes propios al estilo shadcn (sin el CLI completo de shadcn) para mantener velocidad de setup y un look consistente.
- **DELETE de iniciativas**: hard-delete (borra evaluaciones, scores y vetos asociados en cascada). La eliminación es manual desde la UI con confirmación.
- **Inmutabilidad de matrices**: una matriz que ya tiene al menos una `Evaluacion` no se puede editar (409 Conflict). Sí se permite soft-delete (oculta del dropdown pero las evaluaciones históricas la siguen referenciando).
- **Cálculo del puntaje ponderado**: `score × peso` por dimensión, sin invertir aritméticamente en backend. La marca `invertida` es solo para que el front muestre la guía al evaluador (ej. "5 = mínima complejidad").
- **Detección de la dimensión "Esfuerzo"** para decidir Quick Win vs Major Project: primero busca la dimensión con `invertida=true`; si no hay, busca por nombre que contenga "esfuerzo" (case-insensitive); si tampoco encuentra, no clasifica como QUICK_WIN/MAJOR_PROJECT y devuelve A_REVISAR.
- **CORS**: habilitado para `http://localhost:5173` (configurable por properties).

## Estructura del repo

```
.
├── docker-compose.yml
├── README.md
├── backend/
│   ├── pom.xml
│   ├── api.http
│   └── src/main/java/com/correoargentino/evaluador/
│       ├── EvaluadorApplication.java
│       ├── config/    (CorsConfig)
│       ├── domain/    (entidades JPA + enums)
│       ├── repository/
│       ├── dto/
│       ├── service/
│       ├── controller/
│       └── exception/
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── api/
        ├── components/
        ├── pages/
        └── lib/
```
# MatrizPonderada
