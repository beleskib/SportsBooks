import {
  MatchStatus,
  MatchType,
  MatchVisibility,
  ParticipantStatus,
  ParticipantRole,
  RecurrenceFrequency,
  SportType,
  DayOfWeek,
} from '../enums';

// ============================================================
// Match types
// ============================================================

export interface Match {
  id: number;
  hostId: number;
  hostName: string | null;
  hostPhotoUrl: string | null;
  bookingId: number | null;
  venueId: number | null;
  venueName: string | null;
  sportType: SportType;
  matchType: MatchType;
  status: MatchStatus;
  visibility: MatchVisibility;
  title: string;
  description: string | null;
  matchDate: string;
  startTime: string;
  endTime: string;
  minPlayers: number;
  maxPlayers: number;
  currentPlayers: number;
  minSkillLevel: number | null;
  maxSkillLevel: number | null;
  locationName: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  isFree: boolean;
  costPerPlayer: number;
  recurrenceRuleId: number | null;
  parentMatchId: number | null;
  participants: MatchParticipant[];
  createdAt: string;
  updatedAt: string;
}

export interface MatchParticipant {
  id: number;
  matchId: number;
  userId: number;
  userName: string | null;
  userPhotoUrl: string | null;
  status: ParticipantStatus;
  role: ParticipantRole;
  joinedAt: string;
  createdAt: string;
}

export interface MatchChatMessage {
  id: number;
  matchId: number;
  senderId: number;
  senderName: string | null;
  senderPhotoUrl: string | null;
  content: string;
  createdAt: string;
}

export interface PlayerRating {
  id: number;
  matchId: number;
  raterId: number;
  raterName: string | null;
  ratedId: number;
  ratedName: string | null;
  skillRating: number;
  sportsmanshipRating: number;
  punctualityRating: number;
  comment: string | null;
  createdAt: string;
}

export interface MatchRecurrenceRule {
  id: number;
  hostId: number;
  frequency: RecurrenceFrequency;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  sportType: SportType;
  title: string;
  venueId: number | null;
  locationName: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  minPlayers: number;
  maxPlayers: number;
  minSkillLevel: number | null;
  maxSkillLevel: number | null;
  isActive: boolean;
  nextOccurrenceDate: string | null;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Request DTOs
// ============================================================

export interface CreateMatchRequest {
  bookingId?: number;
  venueId?: number;
  sportType: SportType;
  matchType: MatchType;
  visibility?: MatchVisibility;
  title: string;
  description?: string;
  matchDate: string;
  startTime: string;
  endTime: string;
  minPlayers: number;
  maxPlayers: number;
  minSkillLevel?: number;
  maxSkillLevel?: number;
  locationName?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  isFree?: boolean;
  costPerPlayer?: number;
}

export interface UpdateMatchRequest {
  title?: string;
  description?: string;
  matchDate?: string;
  startTime?: string;
  endTime?: string;
  minPlayers?: number;
  maxPlayers?: number;
  minSkillLevel?: number;
  maxSkillLevel?: number;
  locationName?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  isFree?: boolean;
  costPerPlayer?: number;
  visibility?: MatchVisibility;
  status?: MatchStatus;
}

export interface MatchFilters {
  sportType?: SportType;
  status?: MatchStatus;
  minSkillLevel?: number;
  maxSkillLevel?: number;
  matchDate?: string;
  matchType?: MatchType;
  hostId?: number;
}

export interface JoinMatchRequest {
  message?: string;
}

export interface RespondToJoinRequest {
  status: 'approved' | 'declined';
}

export interface SendChatMessageRequest {
  content: string;
}

export interface CreatePlayerRatingRequest {
  ratedId: number;
  skillRating: number;
  sportsmanshipRating: number;
  punctualityRating: number;
  comment?: string;
}

export interface CreateRecurrenceRuleRequest {
  frequency: RecurrenceFrequency;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  sportType: SportType;
  title: string;
  venueId?: number;
  locationName?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  minPlayers: number;
  maxPlayers: number;
  minSkillLevel?: number;
  maxSkillLevel?: number;
}
