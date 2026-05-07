# Evaluador de Iniciativas IA — Correo Argentino

Aplicación full-stack para evaluar iniciativas de IA mediante matrices de decisión ponderada.

## Stack

- **Backend**: Spring Boot 3.3 · Spring Data JPA · Java 17 · Maven
- **Base de datos**: SQL Server 2022 (Flyway para migraciones)
- **Frontend**: React 18 · Vite · React Router · Axios · Tailwind CSS
- **Validación**: Bean Validation (backend) · react-hook-form + zod (frontend)

## Requisitos previos

- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -v`)
- Node 18+ (`node -v`)
- Docker Desktop con compose v2 (`docker compose version`)

## Levantar SQL Server

Desde la raíz del repo:

```bash
docker compose up -d
```

Esto levanta SQL Server 2022 en `localhost:1433` y crea la base `EvaluadorIA` automáticamente vía el contenedor `mssql-init`. Credenciales (solo dev):

- Usuario: `sa`
- Password: `Evaluador#2026`
- Base: `EvaluadorIA`

Para detener:

```bash
docker compose down
```

Para borrar el volumen y empezar de cero:

```bash
docker compose down -v
```

## Backend

```bash
cd backend
mvn spring-boot:run
```

La app escucha en `http://localhost:8080`. Flyway corre las migraciones automáticamente al levantar (incluye seed de la matriz inicial "Evaluador Correo Argentino — 6 dimensiones").

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
