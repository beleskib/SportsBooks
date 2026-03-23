import { apiClient } from './client'
import type { ApiResponse, Venue, VenueImage, CreateVenueRequest, UpdateVenueRequest } from '@/types'

export const venueApi = {
  getAll: () =>
    apiClient.get('/venues') as Promise<ApiResponse<Venue[]>>,

  getMine: () =>
    apiClient.get('/venues/mine') as Promise<ApiResponse<Venue[]>>,

  getById: (id: number) =>
    apiClient.get(`/venues/${id}`) as Promise<ApiResponse<Venue>>,

  create: (data: CreateVenueRequest) =>
    apiClient.post('/venues', data) as Promise<ApiResponse<Venue>>,

  update: (id: number, data: UpdateVenueRequest) =>
    apiClient.put(`/venues/${id}`, data) as Promise<ApiResponse<Venue>>,

  delete: (id: number) =>
    apiClient.delete(`/venues/${id}`) as Promise<ApiResponse<{ id: number }>>,

  addImage: (venueId: number, data: { imageUrl: string; isPrimary?: boolean; displayOrder?: number }) =>
    apiClient.post(`/venues/${venueId}/images`, data) as Promise<ApiResponse<VenueImage>>,

  deleteImage: (venueId: number, imageId: number) =>
    apiClient.delete(`/venues/${venueId}/images/${imageId}`) as Promise<ApiResponse<{ id: number }>>,

  setPrimaryImage: (venueId: number, imageId: number) =>
    apiClient.put(`/venues/${venueId}/images/${imageId}/primary`) as Promise<ApiResponse<{ imageId: number }>>,
}
