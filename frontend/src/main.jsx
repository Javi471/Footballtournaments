// ─────────────────────────────────────────────────────────────
// main.jsx — Punto de entrada de React para localhost:5173
// Solo se usa en modo desarrollo. En la app real (localhost:8080)
// este archivo NO se ejecuta — se usa classifica.jsx en su lugar
// ─────────────────────────────────────────────────────────────

// Importa React — necesario para que JSX funcione
import React from 'react'

// createRoot: función que conecta React con un elemento del HTML
import { createRoot } from 'react-dom/client'

// App: el componente principal que contiene la página de clasificación
import App from './App.jsx'

// Busca el <div id="root"> en index.html y monta el componente App dentro
// A partir de aquí React controla todo lo que hay dentro de ese div
createRoot(document.getElementById('root')).render(<App />)
