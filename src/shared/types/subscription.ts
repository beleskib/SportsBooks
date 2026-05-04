// ============================================================
// SportsBooks+ Subscription types
// ============================================================

export type SubscriptionStatus = 'trialing' | 'active' | 'past_due' | 'canceled' | 'unpaid';
export type ProfileVisibility = 'public' | 'friends_only' | 'private';

export interface SubscriptionInfo {
  id: number;
  status: SubscriptionStatus;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  trialEnd: string | null;
}

export interface SubscriptionStatusResponse {
  isPlus: boolean;
  subscription: SubscriptionInfo | null;
  profileVisibility: ProfileVisibility;
}

export interface CheckoutResponse {
  checkoutUrl: string;
  sessionId: string;
}

export interface SetVisibilityRequest {
  visibility: ProfileVisibility;
}
