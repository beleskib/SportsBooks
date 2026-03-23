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
  SkillLevel,
  ExperienceDuration,
  MatchStatus,
  MatchType,
  MatchVisibility,
  ParticipantStatus,
  ParticipantRole,
  RecurrenceFrequency,
  StripeOnboardingStatus,
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
  UserSportExpertise,
  CreateUserRequest,
  UpdateUserRequest,
  SetRoleRequest,
  CompleteOnboardingRequest,
  SetSportExpertiseRequest,
  PublicPlayerProfile,
  PublicMatchSummary,
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
  StripeConnectOnboardingResponse,
  StripeAccountStatusResponse,
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

// Match types
export type {
  Match,
  MatchParticipant,
  MatchChatMessage,
  PlayerRating,
  MatchRecurrenceRule,
  CreateMatchRequest,
  UpdateMatchRequest,
  MatchFilters,
  JoinMatchRequest,
  RespondToJoinRequest,
  SendChatMessageRequest,
  CreatePlayerRatingRequest,
  CreateRecurrenceRuleRequest,
} from './types/match';

// Dashboard types
export type {
  PartnerDashboardStats,
  MonthlyRevenue,
  BookingStatusCount,
  AdminDashboardStats,
  SportPopularity,
} from './types/dashboard';

// Notification types
export type {
  Notification,
  NotificationType,
  RegisterDeviceTokenRequest,
  MarkNotificationsReadRequest,
} from './types/notification';

// Search types
export type {
  GlobalSearchResults,
} from './types/search';

// Favorite types
export type {
  Favorite,
  FavoriteEntityType,
  ToggleFavoriteRequest,
  CheckFavoritesRequest,
  CheckFavoritesResponse,
} from './types/favorite';

// Friendship types
export type {
  Friendship,
  FriendshipStatus,
  SendFriendRequestRequest,
  RespondToFriendRequestRequest,
  UserSearchResult,
} from './types/friendship';

// Party types
export type {
  Party,
  PartyMember,
  PartyStatus,
  PartyMemberStatus,
  CreatePartyRequest,
  InviteToPartyRequest,
  RespondToPartyInviteRequest,
  JoinMatchWithPartyRequest,
} from './types/party';

// Available Player types
export type {
  AvailablePlayer,
  RegisterAvailableRequest,
  InviteToMatchRequest,
} from './types/availablePlayer';

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
  NOTIFICATION_ENDPOINTS,
  SEARCH_ENDPOINTS,
  FAVORITE_ENDPOINTS,
  FRIEND_ENDPOINTS,
  STRIPE_CONNECT_ENDPOINTS,
  PARTY_ENDPOINTS,
  AVAILABLE_PLAYER_ENDPOINTS,
} from './api/endpoints';
