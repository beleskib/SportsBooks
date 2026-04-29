import { ExperienceDuration, PartnerType, SkillLevel, SportType, StripeOnboardingStatus, UserRole } from '../enums';

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
  bio: string | null;
  dateOfBirth: string | null;
  onboardingCompleted: boolean;
  role: UserRole;
  partnerType: PartnerType | null;
  isActive: boolean;
  stripeAccountId: string | null;
  stripeOnboardingStatus: StripeOnboardingStatus;
  stripePayoutsEnabled: boolean;
  interestedSports: SportType[];
  sportExpertise: UserSportExpertise[];
  avgPlayerSkillRating: number;
  avgPlayerSportsmanshipRating: number;
  avgPlayerPunctualityRating: number;
  totalPlayerRatings: number;
  totalMatchesPlayed: number;
  // v2-practical-ux: simple 1-5 overall skill level used for quick lobby filtering.
  // Distinct from the per-sport `sportExpertise` list which remains for detailed profiles.
  skillLevel: number | null;
  noShowCount: number;
  totalAttended: number;
  reliabilityScore: number; // 0.00 – 1.00; generated column in DB
  createdAt: string;
  updatedAt: string;
}

export interface UserSportExpertise {
  id: number;
  userId: number;
  sportType: SportType;
  skillLevel: SkillLevel;
  experienceDuration: ExperienceDuration;
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
  bio?: string;
  dateOfBirth?: string;
  // v2-practical-ux
  skillLevel?: number; // 1-5
}

export interface SetRoleRequest {
  role: UserRole;
  partnerType?: PartnerType;
}

export interface CompleteOnboardingRequest {
  displayName?: string;
  photoUrl?: string;
  dateOfBirth?: string;
  bio?: string;
  interestedSports: SportType[];
  expertise: Array<{
    sportType: SportType;
    skillLevel: SkillLevel;
    experienceDuration: ExperienceDuration;
  }>;
}

export interface SetSportExpertiseRequest {
  expertise: Array<{
    sportType: SportType;
    skillLevel: SkillLevel;
    experienceDuration: ExperienceDuration;
  }>;
}

// ============================================================
// Public Player Profile — visible to other players
// ============================================================

export interface PublicPlayerProfile {
  id: number;
  displayName: string | null;
  photoUrl: string | null;
  bio: string | null;
  interestedSports: SportType[];
  sportExpertise: UserSportExpertise[];
  avgPlayerSkillRating: number;
  avgPlayerSportsmanshipRating: number;
  avgPlayerPunctualityRating: number;
  totalPlayerRatings: number;
  totalMatchesPlayed: number;
  skillLevel: number | null;
  reliabilityScore: number;
  recentMatches: PublicMatchSummary[];
  createdAt: string;
}

export interface PublicMatchSummary {
  id: number;
  title: string;
  sportType: SportType;
  matchDate: string;
  status: string;
}
