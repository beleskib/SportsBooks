import { SportType } from '../enums';
import { Discount } from './discount';

// ============================================================
// Coach types
// ============================================================

export interface Coach {
  id: number;
  userId: number;
  name: string;
  bio: string | null;
  sportType: SportType;
  specialization: string | null;
  experienceYears: number;
  pricePerHour: number;
  address: string | null;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  phoneNumber: string | null;
  email: string | null;
  avgRating: number;
  totalReviews: number;
  isActive: boolean;
  images: CoachImage[];
  certifications: CoachCertification[];
  activeDiscount: Discount | null;
  createdAt: string;
  updatedAt: string;
}

export interface CoachImage {
  id: number;
  coachId: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface CoachCertification {
  id: number;
  coachId: number;
  name: string;
  issuingBody: string | null;
  yearObtained: number | null;
  certificateUrl: string | null;
}

export interface CreateCoachRequest {
  name: string;
  bio?: string;
  sportType: SportType;
  specialization?: string;
  experienceYears?: number;
  pricePerHour: number;
  address?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  phoneNumber?: string;
  email?: string;
  certifications?: CreateCoachCertificationRequest[];
}

export interface UpdateCoachRequest {
  name?: string;
  bio?: string;
  specialization?: string;
  experienceYears?: number;
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

export interface CreateCoachCertificationRequest {
  name: string;
  issuingBody?: string;
  yearObtained?: number;
  certificateUrl?: string;
}
