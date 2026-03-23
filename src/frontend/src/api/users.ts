import { apiClient } from './client'
import type { ApiResponse, BackendUser } from '@/types'

export const userApi = {
  getMe: () =>
    apiClient.get('/users/me') as Promise<ApiResponse<BackendUser>>,
}
