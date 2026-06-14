// ─────────────────────────────────────────────────────────────
// App.jsx — Página completa de clasificación para localhost:5173
// Contiene la cabecera (título + botón volver) y usa Classifica.jsx para la tabla
// Solo se usa en desarrollo — en la app real se usa classifica.jsx
// ─────────────────────────────────────────────────────────────

// React: base de React
// useEffect: ejecuta código cuando algo cambia (al cargar la página)
// useState: guarda datos que pueden cambiar y actualiza la pantalla
import React, { useEffect, useState } from 'react'

// Importa el componente que dibuja la tabla (lo comparten App.jsx y classifica.jsx)
import Classifica from './components/Classifica.jsx'

// Lee el ID del torneo de la URL del navegador
// Ejemplo: si la URL es /classifica/3 → devuelve "3"
function getTorneoIdFromUrl() {
  const parts = window.location.pathname.split('/')   // divide la URL por "/"
  const idx = parts.indexOf('classifica')             // busca la palabra "classifica"
  return idx !== -1 ? parts[idx + 1] : null           // devuelve el número que hay después
}

export default function App() {
  // torneoId: número del torneo leído de la URL — no cambia, por eso no tiene setter
  const [torneoId] = useState(getTorneoIdFromUrl)

  // torneoNome: nombre del torneo para mostrarlo en la cabecera — empieza vacío
  const [torneoNome, setTorneoNome] = useState('')

  // useEffect: cuando la página carga, pide la lista de torneos a Spring Boot
  // y busca el nombre del torneo cuyo id coincide con el de la URL
  useEffect(() => {
    if (!torneoId) return                              // si no hay id, no hace nada
    fetch(`/api/tornei`)                               // llama a Spring Boot
      .then(r => r.json())                             // convierte la respuesta a JSON
      .then(tornei => {
        // busca en la lista el torneo cuyo id coincide
        const t = tornei.find(t => String(t.id) === String(torneoId))
        if (t) setTorneoNome(t.nome)                  // guarda el nombre para mostrarlo
      })
      .catch(() => {})                                 // si falla, no muestra nada
  }, [torneoId])                                       // se ejecuta solo cuando cambia torneoId

  // Si no hay torneoId en la URL, redirige a la lista de torneos en Spring Boot
  if (!torneoId) {
    window.location.href = 'http://localhost:8080/tornei'
    return null
  }

  // Devuelve el HTML que React dibuja en el navegador
  return (
    <div style={styles.container}>
      <div style={styles.header}>
        {/* Enlace para volver al detalle del torneo en Spring Boot */}
        <a href={`http://localhost:8080/tornei/${torneoId}`} style={styles.back}>← Volver al torneo</a>

        {/* Título de la página */}
        <h1 style={styles.title}>⚽ Classifica</h1>

        {/* Nombre del torneo — solo se muestra cuando ya se ha cargado */}
        {torneoNome && <h2 style={styles.subtitle}>{torneoNome}</h2>}
      </div>

      {/* Componente que dibuja la tabla — le pasamos el id del torneo */}
      <Classifica torneoId={torneoId} />
    </div>
  )
}

// Estilos en línea (CSS dentro de JavaScript)
const styles = {
  container: { maxWidth: 900, margin: '2rem auto', padding: '0 1.5rem', fontFamily: 'Segoe UI, system-ui, sans-serif' },
  header:    { marginBottom: '1.5rem' },
  title:     { fontSize: '2rem', color: '#1a7d3e', margin: '0.25rem 0' },
  subtitle:  { fontSize: '1.1rem', color: '#6b7280', margin: 0 },
  back:      { color: '#1a7d3e', fontSize: '0.9rem', textDecoration: 'none' },
  error:     { color: '#dc2626', background: '#fee2e2', padding: '12px 16px', borderRadius: 8 },
  hint:      { color: '#6b7280', fontSize: '0.9rem' },
}
