// ============================================================
// API Endpoint Path Constants
// Used by: backend (route definitions), frontend (API calls)
// ============================================================

const API_PREFIX = '/api';

export const AUTH_ENDPOINTS = {
  REGISTER: `${API_PREFIX}/auth/register`,
} as const;

export const USER_ENDPOINTS = {
  ME: `${API_PREFIX}/users/me`,
  SET_ROLE: `${API_PREFIX}/users/me/role`,
} as const;

export const SPORT_ENDPOINTS = {
  LIST: `${API_PREFIX}/sports`,
  BY_ID: (id: number) => `${API_PREFIX}/sports/${id}`,
} as const;

export const VENUE_ENDPOINTS = {
  LIST: `${API_PREFIX}/venues`,
  BY_ID: (id: number) => `${API_PREFIX}/venues/${id}`,
  BY_SPORT: (sportType: string) => `${API_PREFIX}/venues/by-sport/${sportType}`,
  TOP_DEALS: `${API_PREFIX}/venues/top-deals`,
  SEARCH: `${API_PREFIX}/venues/search`,
  MY_VENUES: `${API_PREFIX}/venues/mine`,
  IMAGES: (venueId: number) => `${API_PREFIX}/venues/${venueId}/images`,
  EQUIPMENT: (venueId: number) => `${API_PREFIX}/venues/${venueId}/equipment`,
  TIME_SLOTS: (venueId: number) => `${API_PREFIX}/venues/${venueId}/time-slots`,
} as const;

export const COACH_ENDPOINTS = {
  LIST: `${API_PREFIX}/coaches`,
  BY_ID: (id: number) => `${API_PREFIX}/coaches/${id}`,
  BY_SPORT: (sportType: string) => `${API_PREFIX}/coaches/by-sport/${sportType}`,
  TOP_DEALS: `${API_PREFIX}/coaches/top-deals`,
  SEARCH: `${API_PREFIX}/coaches/search`,
  MY_PROFILE: `${API_PREFIX}/coaches/mine`,
  IMAGES: (coachId: number) => `${API_PREFIX}/coaches/${coachId}/images`,
  CERTIFICATIONS: (coachId: number) => `${API_PREFIX}/coaches/${coachId}/certifications`,
  TIME_SLOTS: (coachId: number) => `${API_PREFIX}/coaches/${coachId}/time-slots`,
} as const;

export const BOOKING_ENDPOINTS = {
  CREATE: `${API_PREFIX}/bookings`,
  LIST: `${API_PREFIX}/bookings`,
  BY_ID: (id: number) => `${API_PREFIX}/bookings/${id}`,
  UPDATE_STATUS: (id: number) => `${API_PREFIX}/bookings/${id}/status`,
  MY_BOOKINGS: `${API_PREFIX}/bookings/mine`,
  PARTNER_BOOKINGS: `${API_PREFIX}/bookings/partner`,
} as const;

export const TIME_SLOT_ENDPOINTS = {
  GENERATE: `${API_PREFIX}/time-slots/generate`,
  BY_ID: (id: number) => `${API_PREFIX}/time-slots/${id}`,
} as const;

export const PAYMENT_ENDPOINTS = {
  CREATE: `${API_PREFIX}/payments`,
  BY_BOOKING: (bookingId: number) => `${API_PREFIX}/payments/booking/${bookingId}`,
} as const;

export const REVIEW_ENDPOINTS = {
  CREATE: `${API_PREFIX}/reviews`,
  FOR_VENUE: (venueId: number) => `${API_PREFIX}/reviews/venue/${venueId}`,
  FOR_COACH: (coachId: number) => `${API_PREFIX}/reviews/coach/${coachId}`,
  MY_REVIEWS: `${API_PREFIX}/reviews/mine`,
} as const;

export const DISCOUNT_ENDPOINTS = {
  CREATE: `${API_PREFIX}/discounts`,
  BY_ID: (id: number) => `${API_PREFIX}/discounts/${id}`,
  FOR_VENUE: (venueId: number) => `${API_PREFIX}/discounts/venue/${venueId}`,
  FOR_COACH: (coachId: number) => `${API_PREFIX}/discounts/coach/${coachId}`,
} as const;

export const DASHBOARD_ENDPOINTS = {
  PARTNER_STATS: `${API_PREFIX}/dashboard/stats`,
  PARTNER_RESERVATIONS: `${API_PREFIX}/dashboard/reservations`,
  PARTNER_BILLINGS: `${API_PREFIX}/dashboard/billings`,
  PARTNER_CALENDAR: `${API_PREFIX}/dashboard/calendar`,
} as const;

export const ADMIN_ENDPOINTS = {
  USERS: `${API_PREFIX}/admin/users`,
  USER_BY_ID: (id: number) => `${API_PREFIX}/admin/users/${id}`,
  BOOKINGS: `${API_PREFIX}/admin/bookings`,
  ANALYTICS: `${API_PREFIX}/admin/analytics`,
  VENUES: `${API_PREFIX}/admin/venues`,
  COACHES: `${API_PREFIX}/admin/coaches`,
} as const;
