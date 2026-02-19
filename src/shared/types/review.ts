// ============================================================
// Review types
// ============================================================

export interface Review {
  id: number;
  playerId: number;
  venueId: number | null;
  coachId: number | null;
  bookingId: number | null;
  rating: number;
  comment: string | null;
  playerName: string | null;
  playerPhotoUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  venueId?: number;
  coachId?: number;
  bookingId?: number;
  rating: number;
  comment?: string;
}
