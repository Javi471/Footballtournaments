# ⚽ Football Tournaments

Sistema web para la gestión de torneos de fútbol amateur.  
Proyecto asignado — SIW (Sistemas Informáticos Web) a.a. 2025/2026.

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.5, Java 21 |
| Persistencia | JPA / Hibernate + PostgreSQL |
| Frontend (principal) | Thymeleaf |
| Frontend (clasificación) | React 18 + Vite |
| Seguridad | Spring Security |
| Base de datos (dev) | Docker + PostgreSQL 15 |

---

## Funcionalidades

### Público (sin login)
- Lista de todos los torneos
- Detalle de torneo (equipos + calendario de partidos)
- Detalle de equipo (plantilla de jugadores)
- Clasificación del torneo (componente React, actualización dinámica)

### Usuarios registrados (USER)
- Ver comentarios en los partidos
- Añadir un comentario en un partido
- Editar su propio comentario

### Administrador (ADMIN)
- Crear y editar torneos (con selección de equipos)
- Crear y editar equipos (con subida de imagen)
- Crear y editar jugadores
- Registrar un partido (con torneo, equipos, árbitro, fecha)
- Insertar el resultado de un partido
- Eliminar equipos y partidos
- Gestión de árbitros

---

## Entidades del dominio

```
Torneo      ←→  Squadra   (many-to-many)
Squadra      →  Giocatore (one-to-many)
Partita      →  Torneo    (many-to-one)
Partita      →  Squadra   (local + visitante)
Partita      →  Arbitro   (many-to-one)
Commento     →  Partita   (many-to-one)
Commento     →  User      (many-to-one)
```

---

## Arranque del proyecto

### 1. Base de datos (Docker)

```bash
docker-compose up -d
```

Crea la base de datos PostgreSQL en `localhost:5432` con:
- BD: `football_db`
- Usuario: `admin`
- Contraseña: `admin123`

### 2. Backend (Spring Boot)

```bash
./mvnw spring-boot:run
```

En Windows:
```cmd
mvnw.cmd spring-boot:run
```

La aplicación arranca en **http://localhost:8080**

Usuarios precargados en el primer arranque:
| Username | Contraseña | Rol |
|----------|------------|-----|
| admin | Admin123 | ADMIN |
| user1 | User123 | USER |

### 3. Frontend React (desarrollo)

```bash
cd frontend
npm install
npm run dev    # → http://localhost:5173
```

Para compilar (genera el bundle en `static/react/`):

```bash
cd frontend
npm run build
```

---

## Estructura del proyecto

```
src/main/java/com/football/tournaments/
├── config/          # SecurityConfig, PasswordConfig, DataInitializer
├── model/           # Entidades JPA (Torneo, Squadra, Giocatore, Partita, Arbitro, Commento, User)
├── repository/      # Interfaces Spring Data JPA
├── service/         # Lógica de negocio (@Transactional)
└── controller/      # Controllers Thymeleaf + REST (clasificación)

src/main/resources/
├── templates/       # Plantillas Thymeleaf
│   ├── torneo/      # Lista, detalle, formulario, clasificación
│   ├── squadra/     # Lista, detalle, formulario
│   ├── partita/     # Detalle, formulario, resultado
│   ├── giocatore/   # Formulario
│   ├── arbitro/     # Lista, formulario
│   ├── commento/    # Formulario de edición
│   └── auth/        # Login, registro
└── static/
    ├── css/         # Estilos globales
    └── react/       # Bundle React compilado (clasificación)

frontend/
└── src/
    └── classifica.jsx   # Componente React para la clasificación dinámica
```

---

## Análisis de rendimiento (requisito 8.2)

En `PartitaRepository` se definen consultas con distintas estrategias:
- `findByTorneoOrderByDataOraAsc` → **LAZY** (problema N+1 potencial)
- `findByTorneoWithTeamsAndReferee` → **JOIN FETCH** (consulta única con join)
- `findByIdWithDetails` → **JOIN FETCH** para partido completo

Resultados obtenidos con un dataset de partidos:

| Estrategia | Tiempo | Consultas |
|---|---|---|
| LAZY (N+1) | 7.477 ms | 7 |
| JOIN FETCH | 2.352 ms | 1 |
| EntityGraph | 5.151 ms | 1 |

**Decisión:** se usa JOIN FETCH porque la consulta es explícita, verificable y siempre más rápida.

---

## Seguridad

- Autenticación con formulario (Spring Security)
- Login con GitHub (OAuth2)
- Contraseñas encriptadas con BCrypt
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Endpoints de admin protegidos con `@PreAuthorize("hasRole('ADMIN')")`
- CSRF activo para páginas Thymeleaf, desactivado para `/api/**`

---

## Entrega

Enviar a `siw.roma3@gmail.com` antes de las 18:00 del día anterior al examen oral.  
Asunto: `[Junio/Julio 2026 PROGETTO DOCENTE] Apellido Matrícula`
