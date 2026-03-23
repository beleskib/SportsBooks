// ============================================================
// Friendship Types — Friends list and friend requests
// ============================================================

export type FriendshipStatus = 'pending' | 'accepted' | 'declined' | 'blocked';

export interface Friendship {
  id: number;
  requesterId: number;
  addresseeId: number;
  status: FriendshipStatus;
  user: {
    id: number;
    displayName: string | null;
    photoUrl: string | null;
  };
  createdAt: string;
  updatedAt: string;
}

export interface SendFriendRequestRequest {
  userId: number;
}

export interface RespondToFriendRequestRequest {
  accept: boolean;
}

export interface UserSearchResult {
  id: number;
  displayName: string | null;
  photoUrl: string | null;
}
