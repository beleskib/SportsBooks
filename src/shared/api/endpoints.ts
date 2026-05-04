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

export const NOTIFICATION_ENDPOINTS = {
  LIST: `${API_PREFIX}/notifications`,
  UNREAD_COUNT: `${API_PREFIX}/notifications/unread-count`,
  MARK_READ: `${API_PREFIX}/notifications/mark-read`,
  DEVICE_TOKEN: `${API_PREFIX}/notifications/device-token`,
} as const;

export const SEARCH_ENDPOINTS = {
  GLOBAL: `${API_PREFIX}/search`,
} as const;

export const FAVORITE_ENDPOINTS = {
  LIST: `${API_PREFIX}/favorites`,
  TOGGLE: `${API_PREFIX}/favorites/toggle`,
  CHECK: `${API_PREFIX}/favorites/check`,
} as const;

export const FRIEND_ENDPOINTS = {
  LIST: `${API_PREFIX}/friends`,
  REQUESTS: `${API_PREFIX}/friends/requests`,
  SEND_REQUEST: `${API_PREFIX}/friends/request`,
  RESPOND: (id: number) => `${API_PREFIX}/friends/request/${id}/respond`,
  REMOVE: (friendId: number) => `${API_PREFIX}/friends/${friendId}`,
  SEARCH_USERS: `${API_PREFIX}/friends/search-users`,
} as const;

export const STRIPE_CONNECT_ENDPOINTS = {
  ONBOARD: `${API_PREFIX}/stripe-connect/onboard`,
  STATUS: `${API_PREFIX}/stripe-connect/status`,
  DASHBOARD_LINK: `${API_PREFIX}/stripe-connect/dashboard-link`,
} as const;

export const PARTY_ENDPOINTS = {
  CREATE: `${API_PREFIX}/parties`,
  MY_ACTIVE: `${API_PREFIX}/parties/active`,
  BY_ID: (id: number) => `${API_PREFIX}/parties/${id}`,
  INVITE: (id: number) => `${API_PREFIX}/parties/${id}/invite`,
  RESPOND: (id: number) => `${API_PREFIX}/parties/${id}/respond`,
  DISBAND: (id: number) => `${API_PREFIX}/parties/${id}/disband`,
} as const;

export const AVAILABLE_PLAYER_ENDPOINTS = {
  LIST: `${API_PREFIX}/available-players`,
  ME: `${API_PREFIX}/available-players/me`,
  REGISTER: `${API_PREFIX}/available-players`,
  UNREGISTER: (sportType: string) => `${API_PREFIX}/available-players/${sportType}`,
  INVITE_TO_MATCH: (matchId: number) => `${API_PREFIX}/matches/${matchId}/invite`,
} as const;

// ============================================================
// v2-practical-ux — unified home feed, unified Play search,
// one-tap rebook, split payments, booking participants
// ============================================================

export const HOME_ENDPOINTS = {
  FEED: `${API_PREFIX}/home/feed`,
} as const;

export const PLAY_ENDPOINTS = {
  SEARCH: `${API_PREFIX}/play/search`,
} as const;

export const REBOOK_ENDPOINTS = {
  FROM_BOOKING: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/rebook`,
} as const;

export const SPLIT_PAYMENT_ENDPOINTS = {
  CREATE: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/split`,
  SUMMARY: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/split`,
  PAY_SHARE: (shareId: number) => `${API_PREFIX}/split-payments/${shareId}/pay`,
} as const;

export const BOOKING_PARTICIPANT_ENDPOINTS = {
  INVITE: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/invite`,
  RESPOND: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/respond`,
  MARK_ATTENDANCE: (bookingId: number) => `${API_PREFIX}/bookings/${bookingId}/attendance`,
} as const;

// ============================================================
// SportsBooks+ subscription management
// ============================================================

export const SUBSCRIPTION_ENDPOINTS = {
  STATUS: `${API_PREFIX}/subscription`,
  CHECKOUT: `${API_PREFIX}/subscription/checkout`,
  CANCEL: `${API_PREFIX}/subscription/cancel`,
  REACTIVATE: `${API_PREFIX}/subscription/reactivate`,
  VISIBILITY: `${API_PREFIX}/subscription/visibility`,
} as const;
