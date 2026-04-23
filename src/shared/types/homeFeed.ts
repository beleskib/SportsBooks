// ============================================================
// Home Feed types — v2-practical-ux
// Single unified endpoint backing the app's home screen.
// Replaces 5+ round trips with one payload.
// ============================================================

import { SportType } from '../enums';
import { Booking } from './booking';

export interface HomeFeedResponse {
  greeting: {
    displayName: string | null;
    reliabilityScore: number;
    totalAttended: number;
  };
  // One-tap rebook cards — recent confirmed/completed bookings
  recentBookings: RebookSuggestion[];
  // Open lobbies/matches that match user's interests and skill level
  suggestedPlay: PlaySuggestion[];
  // Friends that are "available to play" right now
  friendsAvailable: FriendAvailability[];
  // Upcoming bookings (next 7 days)
  upcoming: Booking[];
}

export interface RebookSuggestion {
  bookingId: number;
  venueId: number | null;
  venueName: string | null;
  coachId: number | null;
  coachName: string | null;
  sportType: SportType;
  lastPlayedAt: string;
  lastSlotStart: string; // HH:mm
  price: number;
  timesBooked: number; // how often this player books this venue/slot
}

export interface PlaySuggestion {
  type: 'lobby' | 'match' | 'open_slot';
  id: number;
  title: string;
  sportType: SportType;
  startAt: string;
  venueName: string | null;
  distanceKm: number | null;
  currentPlayers: number;
  maxPlayers: number;
  skillLevelMin: number | null;
  skillLevelMax: number | null;
  price: number | null;
}

export interface FriendAvailability {
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  sportType: SportType;
  skillLevel: number | null;
  availableUntil: string | null;
  distanceKm: number | null;
}
