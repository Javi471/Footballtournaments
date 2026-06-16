# ⚽ Football Tournaments
Web system for managing amateur football tournaments.  
Assigned project — Sistemi Informativi Su Web 2025/2026.

Created by:
JAVIER AGUILERA ARCO
77021357S
MATRICOLA: 652476
---

## Technologies

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5, Java 21 |
| Persistence | JPA / Hibernate + PostgreSQL |
| Frontend (main) | Thymeleaf |
| Frontend (standings) | React 18 + Vite |
| Security | Spring Security |
| Database (dev) | Docker + PostgreSQL 15 |

---

## Features

### Public (no login required)
- List of all tournaments
- Tournament detail (teams + match schedule)
- Team detail (squad of players)
- Tournament standings (React component, dynamic update)

### Registered users (USER)
- View comments on matches
- Add a comment on a match
- Edit their own comment

### Administrator (ADMIN)
- Create and edit tournaments (with team selection)
- Create and edit teams (with image upload)
- Create and edit players
- Register a match (with tournament, teams, referee, date)
- Enter the result of a match
- Delete teams and matches
- Manage referees

---

## Domain entities

```
Torneo      ←→  Squadra   (many-to-many)
Squadra      →  Giocatore (one-to-many)
Partita      →  Torneo    (many-to-one)
Partita      →  Squadra   (home + away)
Partita      →  Arbitro   (many-to-one)
Commento     →  Partita   (many-to-one)
Commento     →  User      (many-to-one)
```

---

## Starting the project

### 1. Database (Docker)

```bash
docker-compose up -d
```

Creates the PostgreSQL database at `localhost:5432` with:
- DB: `football_db`
- User: `admin`
- Password: `admin123`

### 2. Backend (Spring Boot)

```bash
./mvnw spring-boot:run
```

On Windows:
```cmd
mvnw.cmd spring-boot:run
```

The application starts at **http://localhost:8080**

Pre-loaded users on first startup:
| Username | Password | Role |
|----------|----------|------|
| admin | Admin123 | ADMIN |
| user1 | User123 | USER |

### 3. React Frontend (Only used during development)

```bash
cd frontend
npm install
npm run dev    # → http://localhost:5173
```

To compile (generates the bundle in `static/react/`):

```bash
cd frontend
npm run build
```

---

## Project structure

```
src/main/java/com/football/tournaments/
├── config/          # SecurityConfig, PasswordConfig, DataInitializer
├── model/           # JPA entities (Torneo, Squadra, Giocatore, Partita, Arbitro, Commento, User)
├── repository/      # Spring Data JPA interfaces
├── service/         # Business logic (@Transactional)
└── controller/      # Thymeleaf controllers + REST (standings)

src/main/resources/
├── templates/       # Thymeleaf templates
│   ├── torneo/      # List, detail, form, standings
│   ├── squadra/     # List, detail, form
│   ├── partita/     # Detail, form, result
│   ├── giocatore/   # Form
│   ├── arbitro/     # List, form
│   ├── commento/    # Edit form
│   └── auth/        # Login, register
└── static/
    ├── css/         # Global styles
    └── react/       # Compiled React bundle (standings)

frontend/
└── src/
    └── classifica.jsx   # React component for dynamic standings
```

---

## Performance analysis (requirement 8.2)

`PartitaRepository` defines queries with different strategies:
- `findByTorneoOrderByDataOraAsc` → **LAZY** (potential N+1 problem)
- `findByTorneoWithTeamsAndReferee` → **JOIN FETCH** (single query with join)
- `findByIdWithDetails` → **JOIN FETCH** for full match detail

Results obtained with a dataset of matches:

| Strategy | Time | Queries |
|----------|------|---------|
| LAZY (N+1) | 7.477 ms | 7 |
| JOIN FETCH | 2.352 ms | 1 |
| EntityGraph | 5.151 ms | 1 |

**Decision:** JOIN FETCH is used because the query is explicit, verifiable and always faster.

---

## Security

- Form-based authentication (Spring Security)
- GitHub login (OAuth2)
- Passwords encrypted with BCrypt
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Admin endpoints protected with `@PreAuthorize("hasRole('ADMIN')")`
- CSRF enabled for Thymeleaf pages, disabled for `/api/**`
