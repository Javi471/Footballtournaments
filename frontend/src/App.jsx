import React, { useEffect, useState } from 'react'
import Classifica from './components/Classifica.jsx'

// Lee el torneoId de la URL: /classifica/3 → torneoId = 3
function getTorneoIdFromUrl() {
  const parts = window.location.pathname.split('/')
  const idx = parts.indexOf('classifica')
  return idx !== -1 ? parts[idx + 1] : null
}

export default function App() {
  const [torneoId] = useState(getTorneoIdFromUrl)
  const [torneoNome, setTorneoNome] = useState('')

  useEffect(() => {
    if (!torneoId) return
    fetch(`/api/tornei`)
      .then(r => r.json())
      .then(tornei => {
        const t = tornei.find(t => String(t.id) === String(torneoId))
        if (t) setTorneoNome(t.nome)
      })
      .catch(() => {})
  }, [torneoId])

  if (!torneoId) {
    return (
      <div style={styles.container}>
        <p style={styles.error}>⚠ No se ha indicado ningún torneo en la URL.</p>
        <p style={styles.hint}>Accede desde la app principal en <a href="http://localhost:8080/tornei">localhost:8080/tornei</a></p>
      </div>
    )
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <a href={`http://localhost:8080/tornei/${torneoId}`} style={styles.back}>← Volver al torneo</a>
        <h1 style={styles.title}>⚽ Classifica</h1>
        {torneoNome && <h2 style={styles.subtitle}>{torneoNome}</h2>}
      </div>
      <Classifica torneoId={torneoId} />
    </div>
  )
}

const styles = {
  container: { maxWidth: 900, margin: '2rem auto', padding: '0 1.5rem', fontFamily: 'Segoe UI, system-ui, sans-serif' },
  header:    { marginBottom: '1.5rem' },
  title:     { fontSize: '2rem', color: '#1a7d3e', margin: '0.25rem 0' },
  subtitle:  { fontSize: '1.1rem', color: '#6b7280', margin: 0 },
  back:      { color: '#1a7d3e', fontSize: '0.9rem', textDecoration: 'none' },
  error:     { color: '#dc2626', background: '#fee2e2', padding: '12px 16px', borderRadius: 8 },
  hint:      { color: '#6b7280', fontSize: '0.9rem' },
}
