import axios from 'axios'
import { auth } from '@/config/firebase'

const apiClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
})

// Add auth token to every request
apiClient.interceptors.request.use(async (config) => {
  const user = auth.currentUser
  if (user) {
    const token = await user.getIdToken()
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401 Unauthorized — token expired or invalid, redirect to login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — redirect to login
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Unwrap response — backend wraps in { success, data, message }
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const apiError = error.response?.data
    return Promise.reject(
      apiError || { success: false, error: { code: 'NETWORK_ERROR', message: 'Network error' } }
    )
  }
)

export { apiClient }
