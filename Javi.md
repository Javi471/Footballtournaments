# Javi.md — Development Diary

Record of all steps taken during the development of the **Football Tournaments** project.

---

## ✅ Step 1 — Initial project setup
**Date:** 25/05/2026

- Created GitHub repository: https://github.com/Javi471/Football-tournaments.git
- Connected local project to remote repository with `git remote add origin`
- Read the project PDF assigned by the professor (pages 1-6)
- Analysed the reference project structure as a base skeleton

---

## ✅ Step 2 — Project structure and configuration
**Date:** 25/05/2026

- Created `pom.xml` with Spring Boot 3.5, JPA, Security, Thymeleaf, PostgreSQL, Lombok, Dotenv dependencies
- Configured `application.properties` with DB parameters via environment variables (`.env`)
- Created `docker-compose.yml` for PostgreSQL 15 database
- Added appropriate `.gitignore` for Java/Maven/Node

---

## ✅ Step 3 — Data model (JPA entities)
**Date:** 25/05/2026

Entities created in the `model/` package:
- `Torneo` — name, year, description → many-to-many relationship with Squadra
- `Squadra` — name, annoFondazione, city → one-to-many relationship with Giocatore
- `Giocatore` — name, surname, birthDate, position, height → belongs to Squadra
- `Partita` — dateTime, location, homeGoals, awayGoals, status (SCHEDULED/PLAYED/CANCELLED)
- `Arbitro` — name, surname, refereeCode
- `Commento` — text, creationDate → connected to User and Partita
- `User` — username, password (BCrypt), role (USER/ADMIN)

---

## ✅ Step 4 — Repositories (persistence layer)
**Date:** 25/05/2026

Repositories created with Spring Data JPA:
- `TorneoRepository` — custom queries with `JOIN FETCH` for teams and matches
- `SquadraRepository` — `findByIdWithGiocatori` (JOIN FETCH anti-N+1)
- `PartitaRepository` — multiple queries: LAZY, JOIN FETCH, EntityGraph (for performance analysis)
- `GiocatoreRepository`, `ArbitroRepository`, `CommentoRepository`, `UserRepository`

---

## ✅ Step 5 — Service layer (business logic)
**Date:** 25/05/2026

Services created with `@Transactional` annotations:
- Read-only operations → `@Transactional(readOnly = true)`
- Write operations → `@Transactional`
- `PartitaService` includes standings calculation (points, wins, draws, losses, goal difference)
- `UserService` implements `UserDetailsService` for Spring Security

---

## ✅ Step 6 — Controller layer
**Date:** 25/05/2026

Controllers created:
- `AuthController` — login, registration
- `TorneoController` — tournament CRUD (public + admin), standings integration
- `SquadraController` — team CRUD (public + admin)
- `GiocatoreController` — player CRUD (admin only)
- `PartitaController` — match registration, result entry, detail with comments
- `ArbitroController` — referee CRUD (admin only)
- `CommentoController` — add and edit comments (registered users)
- `ClassificaRestController` — REST API `/api/tornei/{id}/classifica` for React

---

## ✅ Step 7 — Security
**Date:** 25/05/2026

- `SecurityConfig` with rules for USER and ADMIN roles
- `PasswordConfig` with BCryptPasswordEncoder
- Public endpoints: `/tornei/**`, `/squadre/**`, `/partite/**` (GET), `/api/tornei/**`
- Protected endpoints: `/commenti/**` (authenticated), `/admin/**` (ADMIN)
- CSRF enabled for Thymeleaf, ignored for `/api/**`

---

## ✅ Step 8 — Thymeleaf templates (Frontend)
**Date:** 25/05/2026

Templates created:
- `fragments/layout.html` — common navbar with Thymeleaf Security
- `auth/login.html`, `auth/register.html`
- `torneo/lista.html`, `torneo/dettaglio.html`, `torneo/form.html`, `torneo/classifica.html`
- `squadra/lista.html`, `squadra/dettaglio.html`, `squadra/form.html`
- `giocatore/form.html`
- `partita/dettaglio.html` (with comments section), `partita/form.html`, `partita/risultato.html`
- `arbitro/lista.html`, `arbitro/form.html`
- `commento/form.html`

---

## ✅ Step 9 — React Frontend (Standings)
**Date:** 25/05/2026

- Created component `frontend/src/classifica.jsx`
- Standings are loaded dynamically via fetch to `/api/tornei/{id}/classifica`
- Table with: position (medals), team, MP, W, D, L, GF, GA, GD, Points
- Build with Vite → output in `src/main/resources/static/react/classifica.js`
- Integration: Thymeleaf template mounts `#classifica-root` with `data-torneo-id`

---

## ✅ Step 10 — Initial data and GitHub
**Date:** 25/05/2026

- `DataInitializer` loads on first startup: admin user, user1, tournaments with real teams (La Liga, Premier League, Serie A), players, referees and matches
- Passwords are encrypted with BCrypt in `DataInitializer` using `passwordEncoder.encode()` before saving to DB (never stored in plain text)
- Created `README.md` with full documentation
- Initial push to GitHub: https://github.com/Javi471/Football-tournaments

---

## ✅ Step 11 — Design system (Dark/teal theme)
**Date:** 26/05/2026

- **Palette**: dark ink (`#161A1F`), teal accent (`#2BB7A8`), paper (`#F4F6F7`)
- **Fonts**: Bebas Neue (headings) + Manrope (body text)
- **`style.css`**: complete rewrite with CSS variables and all components:
  sticky blur navbar, card-grid, table-wrap, colour badges, form-card, alerts, etc.
- **`index.html`**: home page with footballer SVG animation
- **`layout.html`**: navbar with SVG logo, admin pill, registration button
- All templates redesigned with the new system

---

## ✅ Step 12 — JPA performance analysis (requirement 8.2)
**Date:** 27/05/2026

Comparison of 3 strategies for loading matches with their teams and referee:

**Strategy 1 — LAZY (N+1 problem)**
- Loads the list with 1 query, but makes 3 extra queries per match (home team, away team, referee)
- With N matches → `1 + N×3` queries. With 10 matches = 31 queries. Very slow.

**Strategy 2 — JOIN FETCH**
- A single SQL query with JOIN that fetches everything at once → always 1 query

**Strategy 3 — EntityGraph**
- Same result as JOIN FETCH but declarative with annotations, more flexible

**Results obtained:**
```
LAZY (N+1)  →  7.477 ms  │  7 queries  │  1.00x (base)
JOIN FETCH  →  2.352 ms  │  1 query    │  3.18x faster
EntityGraph →  5.151 ms  │  1 query    │  1.45x faster
```
**Decision:** JOIN FETCH is used in repositories because the associations are always the same and the query is explicit and verifiable.

---

## ✅ Step 13 — Image upload for teams (bonus)
**Date:** 13/06/2026

Implementation of the bonus image upload feature for teams:

**Modified files:**
- `Squadra.java` → added `imagePath` field (path to image on disk)
- `SquadraController.java` → added `salvaImmagine()` method that saves the file to `src/main/resources/static/images/squadre/`
- `squadra/form.html` → added `enctype="multipart/form-data"` and `<input type="file">` field
- `squadra/dettaglio.html` → displays the team image in the header
- `squadra/lista.html` → displays the image on each team card
- `application.properties` → added 10MB limit for uploaded files
- Created `src/main/resources/static/images/squadre/.gitkeep` so the folder exists in Git

**Bug fixed (500 on Windows):**
- Problem: `file.transferTo()` fails with relative paths on Windows
- Solution: use `Files.copy(file.getInputStream(), destino, REPLACE_EXISTING)` with absolute path based on `System.getProperty("user.dir")`

---

## ✅ Step 14 — OAuth2 login (GitHub)
**Date:** 13/06/2026

Implementation of the OAuth2 authentication bonus with GitHub:

**Modified files:**
- `pom.xml` → added `spring-boot-starter-oauth2-client` dependency
- `UserRepository.java` → added `findByEmail()` method
- `SecurityConfig.java` → added `.oauth2Login()` with `CustomOAuth2UserService`
- `application.properties` → added GitHub OAuth2 configuration
- `.env` → added `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` variables
- `login.html` → added "Login with GitHub" button

**New file created:**
- `CustomOAuth2UserService.java` → receives GitHub data, looks up the user in the DB by email and creates them if they don't exist with `password="OAUTH2_NO_PASSWORD"` (a placeholder that BCrypt always rejects on normal login)

**Bug fixed (500 on return from GitHub):**
- Problem: `password` column in PostgreSQL was still `NOT NULL` even though we set `null` in Java
- Solution: use placeholder `"OAUTH2_NO_PASSWORD"` instead of `null` → DB accepts the value and BCrypt never validates it as a valid password

**How it works:**
1. User clicks "Login with GitHub"
2. GitHub asks the user for authorisation
3. GitHub redirects back with a code
4. Spring exchanges the code for the user's data (email, name)
5. `CustomOAuth2UserService` finds or creates the user in the DB
6. The user is authenticated with `ROLE_USER`

---

## ✅ Step 15 — Cleanup and fixes
**Date:** 13/06/2026

- `PartitaController.java` → removed unused imports and fields (`@Valid`, `BindingResult`, `userService`, `salvata` variable)
- `application.properties` → removed `spring.devtools.restart.enabled` property not recognised by VS Code
- `pom.xml` → updated Spring Boot from `3.5.0` to `3.5.14` (latest patch version)
- `Hash.java` → removed from project (was a one-time utility to generate BCrypt hashes; passwords are now hashed automatically in `DataInitializer.java`)
- Added Spanish comments across all configuration and test files

---

## ✅ Step 16 — Real React integration in the standings page
**Date:** 14/06/2026

Until now React existed but was not connected: the table was drawn directly by Thymeleaf.
The integration has been completed so that React draws the table:

**Modified files:**
- `frontend/vite.config.js` → added `build` section to compile `classifica.jsx` as an IIFE library
  - Entry: `src/classifica.jsx`
  - Output: `src/main/resources/static/react/classifica.js`
  - Format `iife`: the file runs itself on load, without needing modules
- `torneo/classifica.html` → removed Thymeleaf table; replaced with:
  - `<div id="classifica-root" data-torneo-id="...">` (where React mounts the table)
  - `<script src="/react/classifica.js">` (the compiled React bundle)

**New file generated:**
- `src/main/resources/static/react/classifica.js` → bundle compiled with Vite (React + table included)

**How it works now:**
1. User opens `/tornei/{id}/classifica`
2. Spring Boot returns `classifica.html` (Thymeleaf) with the empty div and tournament id
3. Browser loads and runs `classifica.js`
4. React reads the `data-torneo-id` from the div and calls `/api/tornei/{id}/classifica`
5. Spring Boot responds with JSON (points, wins, goals...)
6. React draws the table inside the div

**Command to recompile React if classifica.jsx is modified:**
```
cd frontend
npm run build
```

---

## ✅ Step 17 — Pagination and filters (bonus)
**Date:** 14/06/2026

**Pagination:**
- `SquadraController` → `findPage(page, 4)` method returns 4 teams per page
- `SquadraService` uses Spring Data JPA's `PageRequest` to paginate the query
- `squadra/lista.html` → Previous / Next buttons generated with Thymeleaf's `#numbers.sequence()`
- Works in both the public view (`/squadre`) and the admin view (`/admin/squadre`)

**Filters (client side):**
- `squadra/dettaglio.html` → filter by position (Goalkeeper, Defender, Midfielder, Forward) and sorting by name/height implemented in JavaScript on the browser side
- No server request needed: filters and sorts the player list already loaded in the HTML

---

## 📋 Pending
- [ ] Unit tests for services
- [ ] Final verification and submission by email to siw.roma3@gmail.com
