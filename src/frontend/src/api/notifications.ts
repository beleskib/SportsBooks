import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface Notification {
  id: number
  userId: number
  type: string
  title: string
  body: string
  data: Record<string, string> | null
  isRead: boolean
  createdAt: string
}

export interface UnreadCountResponse {
  count: number
}

export const notificationApi = {
  getAll: (limit = 20, offset = 0) =>
    apiClient.get(`/notifications?limit=${limit}&offset=${offset}`) as Promise<ApiResponse<Notification[]>>,

  getUnreadCount: () =>
    apiClient.get('/notifications/unread-count') as Promise<ApiResponse<UnreadCountResponse>>,

  markAsRead: (notificationIds: number[]) =>
    apiClient.post('/notifications/mark-read', { notificationIds }) as Promise<ApiResponse<void>>,
}
