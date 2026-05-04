import { apiClient } from './client'
import type {
  SubscriptionStatusResponse,
  CheckoutResponse,
  ProfileVisibility,
} from '@shared/types/subscription'

export const subscriptionApi = {
  /** Current subscription status + profile visibility */
  getStatus: () =>
    apiClient.get<never, { data: SubscriptionStatusResponse }>('/subscription'),

  /** Create a Stripe Checkout session for SportsBooks+ */
  createCheckout: () =>
    apiClient.post<never, { data: CheckoutResponse }>('/subscription/checkout'),

  /** Cancel at end of current billing period */
  cancel: () => apiClient.post<never, { data: { message: string } }>('/subscription/cancel'),

  /** Reactivate a pending cancellation */
  reactivate: () =>
    apiClient.post<never, { data: { message: string } }>('/subscription/reactivate'),

  /** Set profile visibility (Plus-only for non-public) */
  setVisibility: (visibility: ProfileVisibility) =>
    apiClient.put<never, { data: { profileVisibility: ProfileVisibility } }>(
      '/subscription/visibility',
      { visibility },
    ),
}
