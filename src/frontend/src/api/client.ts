import axios from 'axios'
import { auth } from '@/config/firebase'

const apiClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
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
