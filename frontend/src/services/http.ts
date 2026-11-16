import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('sad-evolui-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use((response) => response, (error: unknown) => {
  if (typeof error === 'object' && error !== null && 'response' in error && 'config' in error) {
    const response = (error as { response?: { status?: number }; config?: { url?: string } }).response
    const url = (error as { config?: { url?: string } }).config?.url ?? ''
    if (response?.status === 401 && !url.startsWith('/auth/')) window.dispatchEvent(new Event('sad-evolui-unauthorized'))
  }
  return Promise.reject(error)
})

