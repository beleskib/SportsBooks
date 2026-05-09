import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface DirectMessage {
  id: number
  senderId: number
  receiverId: number
  senderName: string | null
  senderPhotoUrl: string | null
  message: string
  readAt: string | null
  createdAt: string
}

export interface ConversationPreview {
  friendUserId: number
  friendDisplayName: string | null
  friendPhotoUrl: string | null
  lastMessage: string
  lastMessageAt: string
  lastMessageSenderId: number
  unreadCount: number
}

export const dmApi = {
  /** List all conversations (inbox) */
  getConversations: () =>
    apiClient.get('/dm') as Promise<ApiResponse<ConversationPreview[]>>,

  /** Total unread DM count (for badge) */
  getUnreadCount: () =>
    apiClient.get('/dm/unread-count') as Promise<ApiResponse<{ unreadCount: number }>>,

  /** Get messages in a conversation */
  getMessages: (friendUserId: number, limit = 50, offset = 0) =>
    apiClient.get(`/dm/${friendUserId}/messages`, { params: { limit, offset } }) as Promise<ApiResponse<DirectMessage[]>>,

  /** Send a direct message */
  sendMessage: (friendUserId: number, message: string) =>
    apiClient.post(`/dm/${friendUserId}/messages`, { message }) as Promise<ApiResponse<DirectMessage>>,

  /** Mark all messages from a friend as read */
  markRead: (friendUserId: number) =>
    apiClient.put(`/dm/${friendUserId}/read`) as Promise<ApiResponse<{ markedRead: number }>>,
}
