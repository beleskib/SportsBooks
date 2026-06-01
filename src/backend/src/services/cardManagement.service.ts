import { getStripe } from '../config/stripe';
import { query } from '../config/database';
import { NotFoundError, ValidationError } from '../utils/errors';

// ============================================================
// Card Management Service
// Handles Stripe Customer creation, SetupIntents for saving
// cards, listing / deleting saved payment methods, and setting
// a default payment method.
// ============================================================

/**
 * Ensure the user has a Stripe Customer record.
 * Creates one if missing, then persists the ID on the users row.
 */
export async function getOrCreateStripeCustomer(userId: number): Promise<string> {
  // 1. Check if user already has a stripe_customer_id
  const userRes = await query(
    `SELECT stripe_customer_id, email, display_name FROM users WHERE id = $1`,
    [userId],
  );
  if (userRes.rows.length === 0) throw new NotFoundError('User');

  const user = userRes.rows[0];
  if (user.stripe_customer_id) {
    return user.stripe_customer_id;
  }

  // 2. Create a Stripe Customer
  const stripe = getStripe();
  const customer = await stripe.customers.create({
    email: user.email || undefined,
    name: user.display_name || undefined,
    metadata: { sportsbooks_user_id: String(userId) },
  });

  // 3. Save stripe_customer_id to user record
  await query(
    `UPDATE users SET stripe_customer_id = $2, updated_at = NOW() WHERE id = $1`,
    [userId, customer.id],
  );

  return customer.id;
}

/**
 * Create a SetupIntent so the mobile/web client can collect
 * card details via Stripe Elements and save them for future use.
 *
 * Returns the client_secret the frontend needs, plus an
 * ephemeral key so the mobile SDK can access the Customer.
 */
export interface SetupIntentResult {
  setupIntentId: string;
  clientSecret: string;
  customerId: string;
  ephemeralKey: string;
}

export async function createSetupIntent(userId: number): Promise<SetupIntentResult> {
  const customerId = await getOrCreateStripeCustomer(userId);
  const stripe = getStripe();

  // Ephemeral key lets the mobile SDK access the Customer object securely
  const ephemeralKey = await stripe.ephemeralKeys.create(
    { customer: customerId },
    { apiVersion: '2025-02-24.acacia' },
  );

  const setupIntent = await stripe.setupIntents.create({
    customer: customerId,
    // These are the payment method types we allow users to save
    payment_method_types: ['card'],
    metadata: { sportsbooks_user_id: String(userId) },
  });

  return {
    setupIntentId: setupIntent.id,
    clientSecret: setupIntent.client_secret!,
    customerId,
    ephemeralKey: ephemeralKey.secret!,
  };
}

/**
 * List all saved payment methods (cards) for a user.
 */
export interface SavedCard {
  id: string;           // Stripe PaymentMethod ID (pm_xxx)
  brand: string;        // visa, mastercard, amex, etc.
  last4: string;        // last 4 digits
  expMonth: number;
  expYear: number;
  isDefault: boolean;
}

export async function listSavedCards(userId: number): Promise<SavedCard[]> {
  const customerId = await getOrCreateStripeCustomer(userId);
  const stripe = getStripe();

  // Get the customer to find the default payment method
  const customer = await stripe.customers.retrieve(customerId);
  const defaultPmId = typeof customer !== 'string' && !customer.deleted
    ? (customer.invoice_settings?.default_payment_method as string | null)
    : null;

  // List all card payment methods attached to this customer
  const paymentMethods = await stripe.paymentMethods.list({
    customer: customerId,
    type: 'card',
  });

  return paymentMethods.data.map((pm) => ({
    id: pm.id,
    brand: pm.card?.brand ?? 'unknown',
    last4: pm.card?.last4 ?? '????',
    expMonth: pm.card?.exp_month ?? 0,
    expYear: pm.card?.exp_year ?? 0,
    isDefault: pm.id === defaultPmId,
  }));
}

/**
 * Delete (detach) a saved payment method.
 */
export async function deleteSavedCard(userId: number, paymentMethodId: string): Promise<void> {
  const customerId = await getOrCreateStripeCustomer(userId);
  const stripe = getStripe();

  // Verify the payment method belongs to this customer
  const pm = await stripe.paymentMethods.retrieve(paymentMethodId);
  if (pm.customer !== customerId) {
    throw new ValidationError('This payment method does not belong to you');
  }

  await stripe.paymentMethods.detach(paymentMethodId);
}

/**
 * Set a payment method as the customer's default.
 */
export async function setDefaultCard(userId: number, paymentMethodId: string): Promise<void> {
  const customerId = await getOrCreateStripeCustomer(userId);
  const stripe = getStripe();

  // Verify the payment method belongs to this customer
  const pm = await stripe.paymentMethods.retrieve(paymentMethodId);
  if (pm.customer !== customerId) {
    throw new ValidationError('This payment method does not belong to you');
  }

  await stripe.customers.update(customerId, {
    invoice_settings: { default_payment_method: paymentMethodId },
  });
}
