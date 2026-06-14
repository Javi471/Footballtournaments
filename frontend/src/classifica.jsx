// ─────────────────────────────────────────────────────────────
// classifica.jsx — Entry point para la app real (localhost:8080)
// Este es el archivo que Vite compila con "npm run build"
// El resultado (classifica.js) lo carga Thymeleaf en classifica.html
// ─────────────────────────────────────────────────────────────

// React: base de React
// useEffect: ejecuta código cuando algo cambia
// useState: guarda datos que pueden cambiar y actualiza la pantalla
import React, { useEffect, useState } from 'react'

// createRoot: función que conecta React con un elemento del HTML
import { createRoot } from 'react-dom/client'

// ─────────────────────────────────────────────────────────────
// Componente Classifica — dibuja la tabla de clasificación
// Usa las clases CSS del tema oscuro de la app (table-wrap, table, center)
// para que el diseño sea idéntico al que tenía Thymeleaf
// ─────────────────────────────────────────────────────────────
function Classifica({ torneoId }) {

  // classifica: lista de equipos con sus puntos — empieza vacía
  const [classifica, setClassifica] = useState([])

  // loading: true mientras espera la respuesta de Spring Boot
  const [loading, setLoading] = useState(true)

  // error: guarda el mensaje si la llamada falla
  const [error, setError] = useState(null)

  // useEffect: al cargar el componente, pide los datos a Spring Boot
  useEffect(() => {
    if (!torneoId) return                                        // si no hay id, no hace nada

    fetch(`/api/tornei/${torneoId}/classifica`)                  // llama a Spring Boot
      .then(r => {
        if (!r.ok) throw new Error('Error al cargar la clasificación')
        return r.json()                                          // convierte la respuesta a JSON
      })
      .then(data => { setClassifica(data); setLoading(false) }) // guarda los datos y quita el spinner
      .catch(e => { setError(e.message); setLoading(false) })   // guarda el error y quita el spinner
  }, [torneoId])                                                 // se ejecuta cuando cambia torneoId

  // Mientras espera, muestra mensaje de carga
  if (loading) return (
    <p style={{ color: 'var(--muted)', padding: '24px 0' }}>Loading standings…</p>
  )

  // Si hubo un error, muestra el mensaje
  if (error) return (
    <div className="alert alert-danger">⚠ {error}</div>
  )

  // Si no hay equipos, muestra estado vacío
  if (classifica.length === 0) return (
    <div className="empty-state">
      <div className="empty-icon">🏆</div>
      <p>No teams in this tournament yet.</p>
    </div>
  )

  // Si todo bien, dibuja la tabla con las clases CSS del tema oscuro
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {/* Cabecera igual que la tabla Thymeleaf original */}
            <th className="center" style={{ width: 42 }}>#</th>
            <th>Team</th>
            <th className="center">MP</th>
            <th className="center">W</th>
            <th className="center">D</th>
            <th className="center">L</th>
            <th className="center">GF</th>
            <th className="center">GA</th>
            <th className="center">GD</th>
            <th className="center" style={{ fontFamily: "'Bebas Neue',sans-serif", fontSize: 15, color: 'var(--teal-soft)', letterSpacing: '.06em' }}>PTS</th>
          </tr>
        </thead>
        <tbody>
          {/* Recorre la lista y dibuja una fila por equipo */}
          {classifica.map((row, i) => {
            // Diferencia de goles = goles a favor - goles en contra
            const gd = row.golFatti - row.golSubiti
            return (
              // Fondo teal suave para el 1º clasificado
              <tr key={row.squadra.id} style={i === 0 ? { background: 'rgba(43,183,168,.06)' } : {}}>

                {/* Posición: medalla para los 3 primeros, número para el resto */}
                <td className="center">
                  {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉'
                    : <span style={{ color: 'var(--muted)', fontSize: 13 }}>{i + 1}</span>}
                </td>

                {/* Nombre del equipo en blanco y ciudad en gris */}
                <td>
                  <span style={{ fontWeight: 600, color: '#fff' }}>{row.squadra.nome}</span>
                  <span style={{ fontSize: 11, color: 'var(--muted)', marginLeft: 6 }}>{row.squadra.citta}</span>
                </td>

                {/* Partidos jugados */}
                <td className="center">{row.partiteGiocate}</td>

                {/* Victorias en verde */}
                <td className="center">
                  <span style={{ color: '#4ade80', fontWeight: 600 }}>{row.vittorie}</span>
                </td>

                {/* Empates */}
                <td className="center">{row.pareggi}</td>

                {/* Derrotas en rojo */}
                <td className="center">
                  <span style={{ color: '#f87171' }}>{row.sconfitte}</span>
                </td>

                {/* Goles a favor y en contra */}
                <td className="center">{row.golFatti}</td>
                <td className="center">{row.golSubiti}</td>

                {/* Diferencia de goles: verde si positiva, rojo si negativa */}
                <td className="center">
                  <span style={{ color: gd > 0 ? '#4ade80' : gd < 0 ? '#f87171' : 'var(--muted)', fontWeight: gd > 0 ? 600 : 'normal' }}>
                    {gd > 0 ? '+' : ''}{gd}
                  </span>
                </td>

                {/* Puntos en Bebas Neue grande y color teal */}
                <td className="center">
                  <span style={{ fontFamily: "'Bebas Neue',sans-serif", fontSize: 20, color: 'var(--teal-soft)', letterSpacing: '.04em' }}>
                    {row.punti}
                  </span>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>

      {/* Leyenda de abreviaturas igual que la tabla Thymeleaf original */}
      <div style={{ display: 'flex', gap: 20, marginTop: 14, flexWrap: 'wrap', fontSize: 12, color: 'var(--muted)', padding: '0 4px' }}>
        <span><strong style={{ color: '#fff' }}>MP</strong> — Matches Played</span>
        <span><strong style={{ color: '#4ade80' }}>W</strong> — Wins</span>
        <span><strong style={{ color: '#fff' }}>D</strong> — Draws</span>
        <span><strong style={{ color: '#f87171' }}>L</strong> — Losses</span>
        <span><strong style={{ color: '#fff' }}>GF / GA</strong> — Goals For / Against</span>
        <span><strong style={{ color: '#fff' }}>GD</strong> — Goal Difference</span>
        <span><strong style={{ color: 'var(--teal-soft)' }}>PTS</strong> — Points</span>
      </div>
    </div>
  )
}

// ─────────────────────────────────────────────────────────────
// Montaje en el DOM — conecta React con el div de Thymeleaf
// ─────────────────────────────────────────────────────────────

// Busca el div con id="classifica-root" que pone Thymeleaf en classifica.html
const el = document.getElementById('classifica-root')

if (el) {
  // Lee el número del torneo del atributo data-torneo-id del div
  // Thymeleaf lo escribe así: <div id="classifica-root" data-torneo-id="3">
  const torneoId = el.getAttribute('data-torneo-id')

  // Monta el componente Classifica dentro del div y le pasa el torneoId
  createRoot(el).render(<Classifica torneoId={torneoId} />)
}
