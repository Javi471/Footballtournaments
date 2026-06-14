import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({

  // Activa el soporte para React en Vite (sin esto no entiende los archivos .jsx)
  plugins: [react()],

  server: {
    // React corre en localhost:5173
    port: 5173,
    // Si el puerto 5173 esta ocupado por otro programa, da error en vez de buscar otro puerto
    strictPort: true,
    // Si la URL que escribes no existe, carga index.html igualmente (necesario para React)
    historyApiFallback: true,

    // Proxy = intermediario entre React y Spring Boot
    // React esta en el puerto 5173 y Spring Boot en el 8080
    // Sin esto React no podria pedir datos a Spring Boot
    proxy: {
      // Cualquier peticion que empiece por /api la redirige a Spring Boot en el 8080
      // Ejemplo: React llama a /api/tornei/1/classifica
      //          Vite lo redirige a localhost:8080/api/tornei/1/classifica
      //          Spring Boot responde con los datos
      //          Vite se los pasa a React
      '/api': {
        target: 'http://localhost:8080',
        // Necesario para que Spring Boot acepte la peticion redirigida
        changeOrigin: true,
      }
    }
  },

  // Sustituye process.env.NODE_ENV por "production" durante el build
  // Sin esto el navegador da error "process is not defined" porque process es de Node.js
  define: {
    'process.env.NODE_ENV': '"production"'
  },

  // Configuracion del build (compilacion) para produccion
  // Convierte classifica.jsx en classifica.js que puede cargar el navegador
  build: {
    lib: {
      // Archivo de entrada: classifica.jsx (el que tiene el componente + el mount)
      entry: 'src/classifica.jsx',
      // Nombre global de la libreria en el navegador (necesario para formato iife)
      name: 'Classifica',
      // formato iife = el archivo se ejecuta solo al cargarse, sin necesitar import/require
      formats: ['iife'],
      // El archivo de salida se llamara siempre classifica.js
      fileName: () => 'classifica.js'
    },
    // Carpeta de salida: va directamente a static de Spring Boot para que lo sirva
    outDir: '../src/main/resources/static/react',
    // No borra otros archivos que haya en esa carpeta al compilar
    emptyOutDir: false,
  }
})
