import { SportType } from '../enums';

// ============================================================
// Venue types
// ============================================================

export interface Venue {
  id: number;
  ownerId: number;
  name: string;
  description: string | null;
  sportType: SportType;
  pricePerHour: number;
  address: string;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  phoneNumber: string | null;
  email: string | null;
  avgRating: number;
  totalReviews: number;
  isActive: boolean;
  images: VenueImage[];
  equipment: VenueEquipment[];
  activeDiscount: Discount | null;
  createdAt: string;
  updatedAt: string;
}

export interface VenueImage {
  id: number;
  venueId: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface VenueEquipment {
  id: number;
  venueId: number;
  name: string;
  description: string | null;
  isIncluded: boolean;
}

export interface CreateVenueRequest {
  name: string;
  description?: string;
  sportType: SportType;
  pricePerHour: number;
  address: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  phoneNumber?: string;
  email?: string;
  equipment?: CreateVenueEquipmentRequest[];
}

export interface UpdateVenueRequest {
  name?: string;
  description?: string;
  pricePerHour?: number;
  address?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  phoneNumber?: string;
  email?: string;
  isActive?: boolean;
}

export interface CreateVenueEquipmentRequest {
  name: string;
  description?: string;
  isIncluded: boolean;
}

// Imported here to avoid circular deps — Discount is shared
import { Discount } from './discount';
export type { Discount as VenueDiscount };
