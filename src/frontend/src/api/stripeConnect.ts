import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface StripeConnectOnboardingResponse {
  onboardingUrl: string
  stripeAccountId: string
}

export interface StripeAccountStatusResponse {
  stripeAccountId: string | null
  onboardingStatus: 'not_started' | 'pending' | 'complete'
  payoutsEnabled: boolean
  dashboardUrl: string | null
}

export interface StripeDashboardLinkResponse {
  dashboardUrl: string
}

export const stripeConnectApi = {
  /** Start Stripe Connect onboarding — creates an Express account and returns the onboarding URL */
  onboard: () =>
    apiClient.post('/stripe-connect/onboard') as Promise<ApiResponse<StripeConnectOnboardingResponse>>,

  /** Get current Stripe Connect account status for the authenticated partner */
  getStatus: () =>
    apiClient.get('/stripe-connect/status') as Promise<ApiResponse<StripeAccountStatusResponse>>,

  /** Get a login link to the partner's Stripe Express Dashboard */
  getDashboardLink: () =>
    apiClient.post('/stripe-connect/dashboard-link') as Promise<ApiResponse<StripeDashboardLinkResponse>>,
}
