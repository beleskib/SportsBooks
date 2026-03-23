// ============================================================
// Shared types — copied from src/shared/ for frontend use
// ============================================================

// User roles & types
export enum UserRole {
  PLAYER = 'player',
  PARTNER = 'partner',
  ADMIN = 'admin',
}

export enum PartnerType {
  COACH = 'coach',
  VENUE_OWNER = 'venue_owner',
}

export interface BackendUser {
  id: number
  firebaseUid: string
  email: string
  displayName: string | null
  role: UserRole
  partnerType: PartnerType | null
  stripeAccountId: string | null
  stripeOnboardingStatus: string
  stripePayoutsEnabled: boolean
}

// Time slots
export interface TimeSlot {
  id: number
  venueId: number | null
  coachId: number | null
  slotDate: string
  startTime: string
  endTime: string
  isAvailable: boolean
  priceOverride: number | null
}

export enum SportType {
  BASKETBALL = 'basketball',
  FOOTBALL = 'football',
  TENNIS = 'tennis',
  PADDLE = 'paddle',
  VOLLEYBALL = 'volleyball',
  SWIMMING = 'swimming',
  BOXING = 'boxing',
  MMA = 'mma',
  YOGA = 'yoga',
  PILATES = 'pilates',
  CROSSFIT = 'crossfit',
  RUNNING = 'running',
  CYCLING = 'cycling',
  GOLF = 'golf',
  BADMINTON = 'badminton',
  TABLE_TENNIS = 'table_tennis',
  HANDBALL = 'handball',
  BASEBALL = 'baseball',
  CRICKET = 'cricket',
}

export const SPORT_TYPE_LABELS: Record<SportType, string> = {
  [SportType.BASKETBALL]: 'Basketball',
  [SportType.FOOTBALL]: 'Football',
  [SportType.TENNIS]: 'Tennis',
  [SportType.PADDLE]: 'Paddle',
  [SportType.VOLLEYBALL]: 'Volleyball',
  [SportType.SWIMMING]: 'Swimming',
  [SportType.BOXING]: 'Boxing',
  [SportType.MMA]: 'MMA',
  [SportType.YOGA]: 'Yoga',
  [SportType.PILATES]: 'Pilates',
  [SportType.CROSSFIT]: 'CrossFit',
  [SportType.RUNNING]: 'Running',
  [SportType.CYCLING]: 'Cycling',
  [SportType.GOLF]: 'Golf',
  [SportType.BADMINTON]: 'Badminton',
  [SportType.TABLE_TENNIS]: 'Table Tennis',
  [SportType.HANDBALL]: 'Handball',
  [SportType.BASEBALL]: 'Baseball',
  [SportType.CRICKET]: 'Cricket',
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

export interface ApiError {
  success: false
  error: {
    code: string
    message: string
    details?: Record<string, string[]>
  }
}

// Venue types
export interface Venue {
  id: number
  ownerId: number
  name: string
  description: string | null
  sportType: SportType
  pricePerHour: number
  address: string
  city: string | null
  country: string | null
  latitude: number | null
  longitude: number | null
  phoneNumber: string | null
  email: string | null
  avgRating: number
  totalReviews: number
  isActive: boolean
  images: VenueImage[]
  equipment: VenueEquipment[]
  activeDiscount: Discount | null
  createdAt: string
  updatedAt: string
}

export interface VenueImage {
  id: number
  venueId: number
  imageUrl: string
  isPrimary: boolean
  displayOrder: number
}

export interface VenueEquipment {
  id: number
  venueId: number
  name: string
  description: string | null
  isIncluded: boolean
}

export interface CreateVenueRequest {
  name: string
  description?: string
  sportType: SportType
  pricePerHour: number
  address: string
  city?: string
  country?: string
  latitude?: number
  longitude?: number
  phoneNumber?: string
  email?: string
}

export interface UpdateVenueRequest {
  name?: string
  description?: string
  pricePerHour?: number
  address?: string
  city?: string
  country?: string
  latitude?: number
  longitude?: number
  phoneNumber?: string
  email?: string
  isActive?: boolean
}

// Coach types
export interface Coach {
  id: number
  userId: number
  name: string
  bio: string | null
  sportType: SportType
  specialization: string | null
  experienceYears: number
  pricePerHour: number
  address: string | null
  city: string | null
  country: string | null
  latitude: number | null
  longitude: number | null
  phoneNumber: string | null
  email: string | null
  avgRating: number
  totalReviews: number
  isActive: boolean
  images: CoachImage[]
  certifications: CoachCertification[]
  activeDiscount: Discount | null
  createdAt: string
  updatedAt: string
}

export interface CoachImage {
  id: number
  coachId: number
  imageUrl: string
  isPrimary: boolean
  displayOrder: number
}

export interface CoachCertification {
  id: number
  coachId: number
  name: string
  issuingBody: string | null
  yearObtained: number | null
  certificateUrl: string | null
}

export interface CreateCoachRequest {
  name: string
  bio?: string
  sportType: SportType
  specialization?: string
  experienceYears?: number
  pricePerHour: number
  address?: string
  city?: string
  country?: string
  phoneNumber?: string
  email?: string
}

export interface UpdateCoachRequest {
  name?: string
  bio?: string
  specialization?: string
  experienceYears?: number
  pricePerHour?: number
  address?: string
  city?: string
  country?: string
  phoneNumber?: string
  email?: string
  isActive?: boolean
}

export interface Discount {
  id: number
  venueId: number | null
  coachId: number | null
  title: string
  description: string | null
  discountPercent: number | null
  discountAmount: number | null
  validFrom: string | null
  validUntil: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}
