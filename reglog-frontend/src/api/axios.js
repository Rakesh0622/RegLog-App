import axios from 'axios'

// Dev fallback: localhost backend. Prod (Vercel): same-origin '/api' proxied to Render.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || (import.meta.env.PROD ? '' : 'http://localhost:8080'),
  withCredentials: true,
})

export default api