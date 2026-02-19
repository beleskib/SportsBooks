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

export enum DayOfWeek {
  MONDAY = 'monday',
  TUESDAY = 'tuesday',
  WEDNESDAY = 'wednesday',
  THURSDAY = 'thursday',
  FRIDAY = 'friday',
  SATURDAY = 'saturday',
  SUNDAY = 'sunday',
}
