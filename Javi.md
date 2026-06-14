# Javi.md — Diario de desarrollo

Registro de todos los pasos realizados durante el desarrollo del proyecto **Football Tournaments**.

---

## ✅ Paso 1 — Configuración inicial del proyecto
**Fecha:** 25/05/2026

- Creado el repositorio GitHub: https://github.com/Javi471/Football-tournaments.git
- Conectado el proyecto local al repositorio remoto con `git remote add origin`
- Leído el PDF del proyecto asignado por el profesor (páginas 1-6)
- Analizada la estructura del proyecto de referencia como esqueleto base

---

## ✅ Paso 2 — Estructura del proyecto y configuración
**Fecha:** 25/05/2026

- Creado `pom.xml` con dependencias Spring Boot 3.5, JPA, Security, Thymeleaf, PostgreSQL, Lombok, Dotenv
- Configurado `application.properties` con parámetros de BD via variables de entorno (`.env`)
- Creado `docker-compose.yml` para la base de datos PostgreSQL 15
- Añadido `.gitignore` apropiado para Java/Maven/Node

---

## ✅ Paso 3 — Modelo de datos (entidades JPA)
**Fecha:** 25/05/2026

Entidades creadas en el paquete `model/`:
- `Torneo` — nombre, año, descripción → relación many-to-many con Squadra
- `Squadra` — nombre, annoFondazione, ciudad → relación one-to-many con Giocatore
- `Giocatore` — nombre, apellido, fechaNacimiento, posición, altura → pertenece a Squadra
- `Partita` — fechaHora, lugar, golesLocal, golesVisitante, estado (SCHEDULED/PLAYED/CANCELLED)
- `Arbitro` — nombre, apellido, codigoArbitral
- `Commento` — texto, fechaCreacion → conectado a User y Partita
- `User` — username, contraseña (BCrypt), rol (USER/ADMIN)

---

## ✅ Paso 4 — Repositorios (capa de persistencia)
**Fecha:** 25/05/2026

Repositorios creados con Spring Data JPA:
- `TorneoRepository` — consultas personalizadas con `JOIN FETCH` para equipos y partidos
- `SquadraRepository` — `findByIdWithGiocatori` (JOIN FETCH anti-N+1)
- `PartitaRepository` — múltiples consultas: LAZY, JOIN FETCH, EntityGraph (para análisis de rendimiento)
- `GiocatoreRepository`, `ArbitroRepository`, `CommentoRepository`, `UserRepository`

---

## ✅ Paso 5 — Capa de servicios (lógica de negocio)
**Fecha:** 25/05/2026

Servicios creados con anotaciones `@Transactional`:
- Operaciones de solo lectura → `@Transactional(readOnly = true)`
- Operaciones de escritura → `@Transactional`
- `PartitaService` incluye cálculo de clasificación (puntos, victorias, empates, derrotas, diferencia de goles)
- `UserService` implementa `UserDetailsService` para Spring Security

---

## ✅ Paso 6 — Capa de controladores
**Fecha:** 25/05/2026

Controladores creados:
- `AuthController` — login, registro
- `TorneoController` — CRUD torneos (público + admin), integración clasificación
- `SquadraController` — CRUD equipos (público + admin)
- `GiocatoreController` — CRUD jugadores (solo admin)
- `PartitaController` — registro partido, inserción resultado, detalle con comentarios
- `ArbitroController` — CRUD árbitros (solo admin)
- `CommentoController` — añadir y modificar comentarios (usuarios registrados)
- `ClassificaRestController` — API REST `/api/tornei/{id}/classifica` para React

---

## ✅ Paso 7 — Seguridad
**Fecha:** 25/05/2026

- `SecurityConfig` con reglas para roles USER y ADMIN
- `PasswordConfig` con BCryptPasswordEncoder
- Endpoints públicos: `/tornei/**`, `/squadre/**`, `/partite/**` (GET), `/api/tornei/**`
- Endpoints protegidos: `/commenti/**` (autenticados), `/admin/**` (ADMIN)
- CSRF activado para Thymeleaf, ignorado para `/api/**`

---

## ✅ Paso 8 — Plantillas Thymeleaf (Frontend)
**Fecha:** 25/05/2026

Plantillas creadas:
- `fragments/layout.html` — navbar común con Thymeleaf Security
- `auth/login.html`, `auth/register.html`
- `torneo/lista.html`, `torneo/dettaglio.html`, `torneo/form.html`, `torneo/classifica.html`
- `squadra/lista.html`, `squadra/dettaglio.html`, `squadra/form.html`
- `giocatore/form.html`
- `partita/dettaglio.html` (con sección comentarios), `partita/form.html`, `partita/risultato.html`
- `arbitro/lista.html`, `arbitro/form.html`
- `commento/form.html`

---

## ✅ Paso 9 — Frontend React (Clasificación)
**Fecha:** 25/05/2026

- Creado componente `frontend/src/classifica.jsx`
- La clasificación se carga dinámicamente mediante fetch en `/api/tornei/{id}/classifica`
- Tabla con: posición (medallas), equipo, PJ, V, E, D, GF, GC, DG, Puntos
- Build con Vite → salida en `src/main/resources/static/react/classifica.js`
- Integración: la plantilla Thymeleaf monta `#classifica-root` con `data-torneo-id`

---

## ✅ Paso 10 — Datos iniciales y GitHub
**Fecha:** 25/05/2026

- `DataInitializer` carga al primer arranque: usuario admin, usuario user1, torneos con equipos reales (La Liga, Premier League, Serie A), jugadores, árbitros y partidos
- Las contraseñas se encriptan con BCrypt en `DataInitializer` usando `passwordEncoder.encode()` antes de guardarlas en BD (no se guardan en texto plano)
- Creado `README.md` con documentación completa
- Push inicial en GitHub: https://github.com/Javi471/Football-tournaments

---

## ✅ Paso 11 — Sistema de diseño (Tema oscuro/teal)
**Fecha:** 26/05/2026

- **Paleta**: dark ink (`#161A1F`), teal accent (`#2BB7A8`), paper (`#F4F6F7`)
- **Fuentes**: Bebas Neue (títulos) + Manrope (texto)
- **`style.css`**: reescritura completa con variables CSS y todos los componentes:
  navbar sticky blur, card-grid, table-wrap, badges de colores, form-card, alerts, etc.
- **`index.html`**: página de inicio con animación SVG de futbolista
- **`layout.html`**: navbar con logo SVG, pill de admin, botón de registro
- Todos los templates rediseñados con el nuevo sistema

---

## ✅ Paso 12 — Análisis de rendimiento JPA (punto 8.2)
**Fecha:** 27/05/2026

Comparación de 3 estrategias para cargar partidos con sus equipos y árbitro:

**Estrategia 1 — LAZY (problema N+1)**
- Carga la lista con 1 consulta, pero por cada partido hace 3 consultas extra (equipo local, visitante, árbitro)
- Con N partidos → `1 + N×3` consultas. Con 10 partidos = 31 consultas. Muy lento.

**Estrategia 2 — JOIN FETCH**
- Una sola consulta SQL con JOIN que trae todo de golpe → siempre 1 consulta

**Estrategia 3 — EntityGraph**
- Mismo resultado que JOIN FETCH pero declarativo con anotaciones, más flexible

**Resultados obtenidos:**
```
LAZY (N+1)  →  7.477 ms  │  7 consultas  │  1.00x (base)
JOIN FETCH  →  2.352 ms  │  1 consulta   │  3.18x más rápido
EntityGraph →  5.151 ms  │  1 consulta   │  1.45x más rápido
```
**Decisión:** Se usa JOIN FETCH en los repositorios porque las asociaciones son siempre las mismas y la consulta es explícita y verificable.

---

## ✅ Paso 13 — Subida de imágenes para equipos (bonus)
**Fecha:** 13/06/2026

Implementación del bonus de subida de imágenes para equipos:

**Archivos modificados:**
- `Squadra.java` → añadido campo `imagePath` (ruta de la imagen en disco)
- `SquadraController.java` → añadido método `salvaImmagine()` que guarda el archivo en `src/main/resources/static/images/squadre/`
- `squadra/form.html` → añadido `enctype="multipart/form-data"` y campo `<input type="file">`
- `squadra/dettaglio.html` → muestra la imagen del equipo en la cabecera
- `squadra/lista.html` → muestra la imagen en la tarjeta de cada equipo
- `application.properties` → añadido límite de 10MB para archivos subidos
- Creado `src/main/resources/static/images/squadre/.gitkeep` para que la carpeta exista en Git

**Error corregido (500 en Windows):**
- Problema: `file.transferTo()` falla con rutas relativas en Windows
- Solución: usar `Files.copy(file.getInputStream(), destino, REPLACE_EXISTING)` con ruta absoluta basada en `System.getProperty("user.dir")`

---

## ✅ Paso 14 — Login con OAuth2 (GitHub)
**Fecha:** 13/06/2026

Implementación del bonus de autenticación OAuth2 con GitHub:

**Archivos modificados:**
- `pom.xml` → añadida dependencia `spring-boot-starter-oauth2-client`
- `UserRepository.java` → añadido método `findByEmail()`
- `SecurityConfig.java` → añadido `.oauth2Login()` con `CustomOAuth2UserService`
- `application.properties` → añadida configuración de GitHub OAuth2
- `.env` → añadidas variables `GITHUB_CLIENT_ID` y `GITHUB_CLIENT_SECRET`
- `login.html` → añadido botón "Login with GitHub"

**Archivo nuevo creado:**
- `CustomOAuth2UserService.java` → recibe los datos de GitHub, busca el usuario en la BD por email y si no existe lo crea con `password="OAUTH2_NO_PASSWORD"` (placeholder que BCrypt siempre rechaza en el login normal)

**Error corregido (500 al volver de GitHub):**
- Problema: columna `password` en PostgreSQL seguía siendo `NOT NULL` aunque pusimos `null` en Java
- Solución: usar placeholder `"OAUTH2_NO_PASSWORD"` en vez de `null` → la BD acepta el valor y BCrypt nunca lo valida como contraseña válida

**Cómo funciona:**
1. Usuario hace clic en "Login with GitHub"
2. GitHub pide autorización al usuario
3. GitHub redirige de vuelta con un código
4. Spring intercambia el código por los datos del usuario (email, nombre)
5. `CustomOAuth2UserService` busca/crea el usuario en la BD
6. El usuario queda autenticado con `ROLE_USER`

---

## ✅ Paso 15 — Limpieza y correcciones
**Fecha:** 13/06/2026

- `PartitaController.java` → eliminados imports y campos sin usar (`@Valid`, `BindingResult`, `userService`, variable `salvata`)
- `application.properties` → eliminada propiedad `spring.devtools.restart.enabled` no reconocida por VS Code
- `pom.xml` → actualizado Spring Boot de `3.5.0` a `3.5.14` (última versión de parche)
- `Hash.java` → eliminado del proyecto (era una utilidad de un solo uso para generar hashes BCrypt; las contraseñas ya se hashean automáticamente en `DataInitializer.java`)
- Añadidos comentarios en español en todos los archivos de configuración y test

---

## ✅ Paso 16 — Integración real de React en la página de clasificación
**Fecha:** 14/06/2026

Hasta ahora React existía pero no estaba conectado: la tabla la dibujaba Thymeleaf directamente.
Se ha completado la integración para que sea React quien dibuje la tabla:

**Archivos modificados:**
- `frontend/vite.config.js` → añadida sección `build` para compilar `classifica.jsx` como librería IIFE
  - Entrada: `src/classifica.jsx`
  - Salida: `src/main/resources/static/react/classifica.js`
  - Formato `iife`: el archivo se ejecuta solo al cargarse, sin necesitar módulos
- `torneo/classifica.html` → eliminada la tabla Thymeleaf; sustituida por:
  - `<div id="classifica-root" data-torneo-id="...">` (donde React monta la tabla)
  - `<script src="/react/classifica.js">` (el bundle compilado de React)

**Archivo nuevo generado:**
- `src/main/resources/static/react/classifica.js` → bundle compilado con Vite (React + tabla incluidos)

**Cómo funciona ahora:**
1. Usuario abre `/tornei/{id}/classifica`
2. Spring Boot devuelve `classifica.html` (Thymeleaf) con el div vacío y el id del torneo
3. El navegador carga y ejecuta `classifica.js`
4. React lee el `data-torneo-id` del div y llama a `/api/tornei/{id}/classifica`
5. Spring Boot responde con JSON (puntos, victorias, goles...)
6. React dibuja la tabla dentro del div

**Comando para recompilar React si se modifica classifica.jsx:**
```
cd frontend
npm run build
```

---

## ✅ Paso 17 — Paginación y filtros (bonus)
**Fecha:** 14/06/2026

**Paginación:**
- `SquadraController` → método `findPage(page, 4)` devuelve 4 equipos por página
- `SquadraService` usa `PageRequest` de Spring Data JPA para paginar la consulta
- `squadra/lista.html` → botones Anterior / Siguiente generados con `#numbers.sequence()` de Thymeleaf
- Funciona tanto en la vista pública (`/squadre`) como en la vista admin (`/admin/squadre`)

**Filtros (lado cliente):**
- `squadra/dettaglio.html` → filtro por posición (Portero, Defensa, Centrocampista, Delantero) y ordenación por nombre/altura implementados en JavaScript en el lado del navegador
- No requiere petición al servidor: filtra y ordena la lista de jugadores ya cargada en el HTML

---

## 📋 Pendiente
- [ ] Tests unitarios para los servicios
- [ ] Verificación final y entrega por email a siw.roma3@gmail.com
