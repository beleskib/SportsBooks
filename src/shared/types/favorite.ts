// ============================================================
// Favorites / Wishlist Types
// ============================================================

export type FavoriteEntityType = 'venue' | 'coach' | 'match';

export interface Favorite {
  id: number;
  userId: number;
  entityType: FavoriteEntityType;
  entityId: number;
  createdAt: string;
}

export interface ToggleFavoriteRequest {
  entityType: FavoriteEntityType;
  entityId: number;
}

export interface CheckFavoritesRequest {
  entityType: FavoriteEntityType;
  entityIds: number[];
}

export interface CheckFavoritesResponse {
  favorited: Record<number, boolean>;
}
