import { apiClient } from './client'
import type { ApiResponse, Coach, CoachImage, CreateCoachRequest, UpdateCoachRequest } from '@/types'

export const coachApi = {
  getAll: () =>
    apiClient.get('/coaches') as Promise<ApiResponse<Coach[]>>,

  getMine: () =>
    apiClient.get('/coaches/mine') as Promise<ApiResponse<Coach | null>>,

  getById: (id: number) =>
    apiClient.get(`/coaches/${id}`) as Promise<ApiResponse<Coach>>,

  create: (data: CreateCoachRequest) =>
    apiClient.post('/coaches', data) as Promise<ApiResponse<Coach>>,

  update: (id: number, data: UpdateCoachRequest) =>
    apiClient.put(`/coaches/${id}`, data) as Promise<ApiResponse<Coach>>,

  delete: (id: number) =>
    apiClient.delete(`/coaches/${id}`) as Promise<ApiResponse<{ id: number }>>,

  addImage: (coachId: number, data: { imageUrl: string; isPrimary?: boolean; displayOrder?: number }) =>
    apiClient.post(`/coaches/${coachId}/images`, data) as Promise<ApiResponse<CoachImage>>,

  deleteImage: (coachId: number, imageId: number) =>
    apiClient.delete(`/coaches/${coachId}/images/${imageId}`) as Promise<ApiResponse<{ id: number }>>,

  setPrimaryImage: (coachId: number, imageId: number) =>
    apiClient.put(`/coaches/${coachId}/images/${imageId}/primary`) as Promise<ApiResponse<{ imageId: number }>>,
}
