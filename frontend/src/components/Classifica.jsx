import React, { useEffect, useState } from 'react'

export default function Classifica({ torneoId }) {
  const [classifica, setClassifica] = useState([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState(null)

  useEffect(() => {
    if (!torneoId) return
    fetch(`/api/tornei/${torneoId}/classifica`)
      .then(r => {
        if (!r.ok) throw new Error('Errore nel caricamento della classifica')
        return r.json()
      })
      .then(data => { setClassifica(data); setLoading(false) })
      .catch(e  => { setError(e.message); setLoading(false) })
  }, [torneoId])

  if (loading) return <div style={styles.loading}>⏳ Caricamento classifica…</div>
  if (error)   return <div style={styles.error}>⚠ {error}</div>
  if (classifica.length === 0) return <div style={styles.empty}>Nessuna partita giocata ancora.</div>

  return (
    <div style={styles.wrapper}>
      <table style={styles.table}>
        <thead>
          <tr>
            {['#', 'Squadra', 'PG', 'V', 'P', 'S', 'GF', 'GS', 'DR', 'Punti'].map(h => (
              <th key={h} style={h === 'Squadra' ? { ...styles.th, textAlign: 'left' } : styles.th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {classifica.map((row, i) => {
            const diff = row.golFatti - row.golSubiti
            return (
              <tr key={row.squadra.id} style={i === 0 ? styles.trFirst : i % 2 === 0 ? styles.trEven : styles.trOdd}>
                <td style={styles.tdCenter}>
                  {i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : i + 1}
                </td>
                <td style={styles.tdName}>
                  <strong>{row.squadra.nome}</strong>
                  <span style={styles.city}>{row.squadra.citta}</span>
                </td>
                <td style={styles.tdCenter}>{row.partiteGiocate}</td>
                <td style={styles.tdCenter}>{row.vittorie}</td>
                <td style={styles.tdCenter}>{row.pareggi}</td>
                <td style={styles.tdCenter}>{row.sconfitte}</td>
                <td style={styles.tdCenter}>{row.golFatti}</td>
                <td style={styles.tdCenter}>{row.golSubiti}</td>
                <td style={styles.tdCenter}>{diff > 0 ? '+' : ''}{diff}</td>
                <td style={styles.tdPoints}>{row.punti}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
      <p style={styles.legend}>PG=Partite Giocate · V=Vittorie · P=Pareggi · S=Sconfitte · GF=Gol Fatti · GS=Gol Subiti · DR=Differenza Reti</p>
    </div>
  )
}

const green = '#1a7d3e'
const styles = {
  wrapper:  { overflowX: 'auto' },
  table:    { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,.1)' },
  th:       { background: green, color: '#fff', padding: '10px 14px', textAlign: 'center', fontSize: 13, fontWeight: 600 },
  trFirst:  { background: '#dcfce7' },
  trEven:   { background: '#fff' },
  trOdd:    { background: '#f9fafb' },
  tdCenter: { padding: '10px 14px', textAlign: 'center', fontSize: 14, borderBottom: '1px solid #e5e7eb' },
  tdName:   { padding: '10px 14px', fontSize: 14, borderBottom: '1px solid #e5e7eb' },
  tdPoints: { padding: '10px 14px', textAlign: 'center', fontSize: 16, fontWeight: 700, color: green, borderBottom: '1px solid #e5e7eb' },
  city:     { display: 'block', fontSize: 11, color: '#6b7280' },
  loading:  { padding: 24, color: '#6b7280', textAlign: 'center' },
  error:    { background: '#fee2e2', color: '#991b1b', padding: '12px 16px', borderRadius: 8, margin: 16 },
  empty:    { textAlign: 'center', padding: 32, color: '#6b7280' },
  legend:   { color: '#9ca3af', fontSize: 11, marginTop: 8, textAlign: 'right' },
}
