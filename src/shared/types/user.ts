import { PartnerType, UserRole } from '../enums';

// ============================================================
// User types
// ============================================================

export interface UserProfile {
  id: number;
  firebaseUid: string;
  email: string;
  displayName: string | null;
  photoUrl: string | null;
  phoneNumber: string | null;
  role: UserRole;
  partnerType: PartnerType | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  firebaseUid: string;
  email: string;
  displayName?: string;
  photoUrl?: string;
}

export interface UpdateUserRequest {
  displayName?: string;
  photoUrl?: string;
  phoneNumber?: string;
}

export interface SetRoleRequest {
  role: UserRole;
  partnerType?: PartnerType;
}
