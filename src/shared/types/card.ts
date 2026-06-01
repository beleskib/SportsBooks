// ============================================================
// Card Management Types
// Shared contracts for saved payment methods (cards).
// ============================================================

/** A saved card (Stripe PaymentMethod) */
export interface SavedCard {
  /** Stripe PaymentMethod ID (pm_xxx) */
  id: string;
  /** Card brand: visa, mastercard, amex, etc. */
  brand: string;
  /** Last 4 digits of the card number */
  last4: string;
  /** Expiry month (1-12) */
  expMonth: number;
  /** Expiry year (e.g. 2028) */
  expYear: number;
  /** Whether this is the customer's default payment method */
  isDefault: boolean;
}

/** Response from POST /api/cards/setup-intent */
export interface SetupIntentResponse {
  /** Stripe SetupIntent ID */
  setupIntentId: string;
  /** Client secret for Stripe Elements / mobile SDK */
  clientSecret: string;
  /** Stripe Customer ID */
  customerId: string;
  /** Ephemeral key for mobile SDK Customer access */
  ephemeralKey: string;
}
