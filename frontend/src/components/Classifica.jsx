// ─────────────────────────────────────────────────────────────
// Classifica.jsx — Componente que dibuja la tabla de clasificación
// Recibe el torneoId, llama a Spring Boot y muestra los datos
// Lo usan tanto App.jsx (localhost:5173) como classifica.jsx (localhost:8080)
// ─────────────────────────────────────────────────────────────

// React: base de React
// useEffect: ejecuta código cuando algo cambia (al cargar el componente)
// useState: guarda datos que pueden cambiar y actualiza la pantalla
import React, { useEffect, useState } from 'react'

// Recibe torneoId como parámetro (lo pasa quien usa este componente)
export default function Classifica({ torneoId }) {

  // classifica: lista de equipos con puntos — empieza vacía hasta que llegan los datos
  const [classifica, setClassifica] = useState([])

  // loading: true mientras espera la respuesta de Spring Boot, false cuando llegan los datos
  const [loading, setLoading] = useState(true)

  // error: guarda el mensaje si la llamada a Spring Boot falla
  const [error, setError] = useState(null)

  // useEffect: cuando el componente carga, pide los datos a Spring Boot
  useEffect(() => {
    if (!torneoId) return                                        // si no hay id, no hace nada

    fetch(`/api/tornei/${torneoId}/classifica`)                  // llama a Spring Boot
      .then(r => {
        if (!r.ok) throw new Error('Errore nel caricamento della classifica') // si falla, lanza error
        return r.json()                                          // convierte la respuesta a JSON
      })
      .then(data => { setClassifica(data); setLoading(false) }) // guarda los datos y quita el spinner
      .catch(e => { setError(e.message); setLoading(false) })   // guarda el error y quita el spinner
  }, [torneoId])                                                 // se ejecuta solo cuando cambia torneoId

  // Mientras espera la respuesta, muestra un spinner de carga
  if (loading) return (
    <div style={styles.loading}>
      <span style={styles.spinner}></span>
      Caricamento classifica…
    </div>
  )

  // Si hubo un error al llamar a Spring Boot, muestra el mensaje
  if (error) return (
    <div style={styles.error}>⚠ {error}</div>
  )

  // Si no hay partidos jugados todavía, muestra un mensaje vacío
  if (classifica.length === 0) return (
    <div style={styles.empty}>Nessuna partita giocata ancora.</div>
  )

  // Si todo fue bien, dibuja la tabla con los datos
  return (
    <div style={styles.wrapper}>
      <table style={styles.table}>
        <thead>
          <tr>
            {/* Genera las cabeceras de la tabla a partir de este array */}
            {['#', 'Squadra', 'PG', 'V', 'P', 'S', 'GF', 'GS', 'DR', 'Punti'].map(h => (
              // La columna "Squadra" se alinea a la izquierda, el resto al centro
              <th key={h} style={h === 'Squadra' ? { ...styles.th, textAlign: 'left' } : styles.th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {/* Recorre la lista de equipos y dibuja una fila por cada uno */}
          {classifica.map((row, i) => {
            // Diferencia de goles = goles a favor - goles en contra
            const diff = row.golFatti - row.golSubiti
            // isTop: true si es el primer clasificado (para darle fondo verde)
            const isTop = i === 0
            return (
              // key: identificador único de la fila (obligatorio en listas React)
              // style: fondo verde para el 1º, blanco/gris alternado para el resto
              <tr key={row.squadra.id} style={isTop ? styles.trFirst : (i % 2 === 0 ? styles.trEven : styles.trOdd)}>

                {/* Columna posición: medalla para los 3 primeros, número para el resto */}
                <td style={styles.tdCenter}>
                  {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : i + 1}
                </td>

                {/* Columna equipo: nombre en negrita y ciudad debajo */}
                <td style={styles.tdName}>
                  <strong>{row.squadra.nome}</strong>
                  <span style={styles.city}>{row.squadra.citta}</span>
                </td>

                {/* Columnas de estadísticas: partidos, victorias, empates, derrotas */}
                <td style={styles.tdCenter}>{row.partiteGiocate}</td>
                <td style={styles.tdCenter}>{row.vittorie}</td>
                <td style={styles.tdCenter}>{row.pareggi}</td>
                <td style={styles.tdCenter}>{row.sconfitte}</td>

                {/* Goles a favor y en contra */}
                <td style={styles.tdCenter}>{row.golFatti}</td>
                <td style={styles.tdCenter}>{row.golSubiti}</td>

                {/* Diferencia de goles: añade "+" si es positiva */}
                <td style={styles.tdCenter}>{diff > 0 ? '+' : ''}{diff}</td>

                {/* Puntos: más grandes y en verde */}
                <td style={styles.tdPoints}>{row.punti}</td>
              </tr>
            )
          })}
        </tbody>
      </table>

      {/* Leyenda de las abreviaturas de la cabecera */}
      <p style={styles.legend}>PG=Partite Giocate · V=Vittorie · P=Pareggi · S=Sconfitte · GF=Gol Fatti · GS=Gol Subiti · DR=Differenza Reti</p>
    </div>
  )
}

// Color verde usado en cabecera y puntos
const green = '#1a7d3e'

// Estilos en línea (CSS dentro de JavaScript, sin archivo .css externo)
const styles = {
  wrapper:  { overflowX: 'auto', fontFamily: 'Segoe UI, system-ui, sans-serif' },
  table:    { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,.1)' },
  th:       { background: green, color: '#fff', padding: '10px 14px', textAlign: 'center', fontSize: 13, fontWeight: 600 },
  trFirst:  { background: '#dcfce7' },               // fondo verde claro para el 1º clasificado
  trEven:   { background: '#fff' },                  // filas pares: blanco
  trOdd:    { background: '#f9fafb' },               // filas impares: gris muy claro
  tdCenter: { padding: '10px 14px', textAlign: 'center', fontSize: 14, borderBottom: '1px solid #e5e7eb' },
  tdName:   { padding: '10px 14px', fontSize: 14, borderBottom: '1px solid #e5e7eb', display: 'flex', flexDirection: 'column', gap: 2 },
  tdPoints: { padding: '10px 14px', textAlign: 'center', fontSize: 16, fontWeight: 700, color: green, borderBottom: '1px solid #e5e7eb' },
  city:     { fontSize: 11, color: '#6b7280' },
  loading:  { display: 'flex', alignItems: 'center', gap: 8, padding: 24, color: '#6b7280' },
  error:    { background: '#fee2e2', color: '#991b1b', padding: '12px 16px', borderRadius: 8, margin: 16 },
  empty:    { textAlign: 'center', padding: 32, color: '#6b7280' },
  spinner:  { width: 16, height: 16, border: '2px solid #e5e7eb', borderTopColor: green, borderRadius: '50%', animation: 'spin 0.8s linear infinite' },
  legend:   { color: '#9ca3af', fontSize: 11, marginTop: 8, textAlign: 'right' },
}
