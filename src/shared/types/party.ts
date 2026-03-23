// ============================================================
// Party Types — Squad/party system for group match joining
// ============================================================

import { SportType } from '../enums';

export type PartyStatus = 'forming' | 'ready' | 'in_match' | 'disbanded';
export type PartyMemberStatus = 'invited' | 'accepted' | 'declined';

export interface Party {
  id: number;
  leaderId: number;
  leaderName: string | null;
  leaderPhotoUrl: string | null;
  name: string | null;
  sportType: SportType | null;
  status: PartyStatus;
  matchId: number | null;
  members: PartyMember[];
  createdAt: string;
  updatedAt: string;
}

export interface PartyMember {
  id: number;
  partyId: number;
  userId: number;
  userName: string | null;
  userPhotoUrl: string | null;
  status: PartyMemberStatus;
  respondedAt: string | null;
  createdAt: string;
}

export interface CreatePartyRequest {
  name?: string;
  sportType?: SportType;
}

export interface InviteToPartyRequest {
  userIds: number[];
}

export interface RespondToPartyInviteRequest {
  accept: boolean;
}

export interface JoinMatchWithPartyRequest {
  partyId: number;
}
