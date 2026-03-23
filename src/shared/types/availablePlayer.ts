// ============================================================
// Available Player types (matchmaking)
// ============================================================

export interface AvailablePlayer {
  id: number;
  userId: number;
  sportType: string;
  skillLevel: number | null;
  note: string | null;
  latitude: number | null;
  longitude: number | null;
  availableUntil: string | null;
  createdAt: string;
  updatedAt: string;
  // Joined
  displayName?: string;
  photoUrl?: string;
  email?: string;
}

export interface RegisterAvailableRequest {
  sportType: string;
  skillLevel?: number;
  note?: string;
  latitude?: number;
  longitude?: number;
  availableUntil?: string;
}

export interface InviteToMatchRequest {
  userId: number;
}
