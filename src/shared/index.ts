// ============================================================
// SportsBooks — Shared API Contracts
// Barrel export for all enums, types, and API constants
// ============================================================

// Enums
export {
  UserRole,
  PartnerType,
  BookingStatus,
  PaymentStatus,
  SportType,
  DayOfWeek,
} from './enums';

// Common types
export type {
  ApiResponse,
  PaginatedResponse,
  PaginationMeta,
  ApiError,
  PaginationParams,
} from './types/common';

// User types
export type {
  UserProfile,
  CreateUserRequest,
  UpdateUserRequest,
  SetRoleRequest,
} from './types/user';

// Venue types
export type {
  Venue,
  VenueImage,
  VenueEquipment,
  CreateVenueRequest,
  UpdateVenueRequest,
  CreateVenueEquipmentRequest,
} from './types/venue';

// Coach types
export type {
  Coach,
  CoachImage,
  CoachCertification,
  CreateCoachRequest,
  UpdateCoachRequest,
  CreateCoachCertificationRequest,
} from './types/coach';

// Booking types
export type {
  Booking,
  CreateBookingRequest,
  UpdateBookingStatusRequest,
  BookingFilters,
} from './types/booking';

// Time Slot types
export type {
  TimeSlot,
  TimeSlotFilters,
  GenerateSlotsRequest,
  AvailabilityTemplate,
  CreateAvailabilityTemplateRequest,
} from './types/timeSlot';

// Payment types
export type {
  Payment,
  CreatePaymentRequest,
} from './types/payment';

// Review types
export type {
  Review,
  CreateReviewRequest,
} from './types/review';

// Discount types
export type {
  Discount,
  CreateDiscountRequest,
  UpdateDiscountRequest,
} from './types/discount';

// Dashboard types
export type {
  PartnerDashboardStats,
  MonthlyRevenue,
  BookingStatusCount,
  AdminDashboardStats,
  SportPopularity,
} from './types/dashboard';

// API Endpoints
export {
  AUTH_ENDPOINTS,
  USER_ENDPOINTS,
  SPORT_ENDPOINTS,
  VENUE_ENDPOINTS,
  COACH_ENDPOINTS,
  BOOKING_ENDPOINTS,
  TIME_SLOT_ENDPOINTS,
  PAYMENT_ENDPOINTS,
  REVIEW_ENDPOINTS,
  DISCOUNT_ENDPOINTS,
  DASHBOARD_ENDPOINTS,
  ADMIN_ENDPOINTS,
} from './api/endpoints';
