// ============================================================
// Play Search types — v2-practical-ux
// Unified "I want to play" search: lobbies + matches + open slots
// returned together, ranked by relevance.
// ============================================================

import { SportType } from '../enums';
import { PlaySuggestion } from './homeFeed';

export interface PlaySearchRequest {
  // When (ISO range)
  from: string;
  to: string;
  // Where (optional — nearest first if provided)
  latitude?: number;
  longitude?: number;
  radiusKm?: number;
  // What
  sportType?: SportType;
  // Skill filter
  skillLevelMin?: number;
  skillLevelMax?: number;
  // Only show options the user qualifies for (skill + reliability)
  onlyEligible?: boolean;
}

export interface PlaySearchResponse {
  results: PlaySuggestion[];
  // Breakdown for UI chips: "3 lobbies · 12 players · 8 open courts"
  counts: {
    lobbies: number;
    openSlots: number;
    availablePlayers: number;
  };
}
