// ============================================================
// SportsBooks — Shared Enums
// Used by: backend, frontend, referenced by mobile apps
// ============================================================

export enum UserRole {
  PLAYER = 'player',
  PARTNER = 'partner',
  ADMIN = 'admin',
}

export enum PartnerType {
  COACH = 'coach',
  VENUE_OWNER = 'venue_owner',
}

export enum BookingStatus {
  PENDING = 'pending',
  APPROVED = 'approved',
  CONFIRMED = 'confirmed',
  CANCELLED = 'cancelled',
  COMPLETED = 'completed',
  NO_SHOW = 'no_show',
}

export enum PaymentStatus {
  PENDING = 'pending',
  COMPLETED = 'completed',
  FAILED = 'failed',
  REFUNDED = 'refunded',
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

export enum SkillLevel {
  NEWBIE = 'newbie',
  BEGINNER = 'beginner',
  INTERMEDIATE = 'intermediate',
  SEMI_PRO = 'semi_pro',
  PRO = 'pro',
}

export enum ExperienceDuration {
  LESS_THAN_1_YEAR = 'less_than_1_year',
  ONE_TO_3_YEARS = '1_to_3_years',
  THREE_TO_5_YEARS = '3_to_5_years',
  FIVE_TO_10_YEARS = '5_to_10_years',
  TEN_PLUS_YEARS = '10_plus_years',
}

export enum DayOfWeek {
  MONDAY = 'monday',
  TUESDAY = 'tuesday',
  WEDNESDAY = 'wednesday',
  THURSDAY = 'thursday',
  FRIDAY = 'friday',
  SATURDAY = 'saturday',
  SUNDAY = 'sunday',
}

export enum MatchStatus {
  DRAFT = 'draft',
  OPEN = 'open',
  FULL = 'full',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
}

export enum MatchType {
  VENUE_LINKED = 'venue_linked',
  STANDALONE = 'standalone',
}

export enum MatchVisibility {
  PUBLIC = 'public',
  PRIVATE = 'private',
}

export enum ParticipantStatus {
  PENDING = 'pending',
  APPROVED = 'approved',
  DECLINED = 'declined',
  LEFT = 'left',
}

export enum ParticipantRole {
  HOST = 'host',
  PLAYER = 'player',
}

export enum RecurrenceFrequency {
  WEEKLY = 'weekly',
  BIWEEKLY = 'biweekly',
  MONTHLY = 'monthly',
}

export enum StripeOnboardingStatus {
  NOT_STARTED = 'not_started',
  PENDING = 'pending',
  COMPLETE = 'complete',
}

export enum VenueBookingLobbyStatus {
  OPEN = 'open',
  FULL = 'full',
  BOOKING_PENDING = 'booking_pending',
  BOOKING_APPROVED = 'booking_approved',
  PAYMENT_IN_PROGRESS = 'payment_in_progress',
  CONFIRMED = 'confirmed',
  CANCELLED = 'cancelled',
  EXPIRED = 'expired',
}

export enum LobbyPaymentType {
  SPLIT = 'split',
  CREATOR_PAYS = 'creator_pays',
  SPLIT_TO_TEAMS = 'split_to_teams',
}
