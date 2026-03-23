import { getStripe } from '../config/stripe';
import { env } from '../config/env';
import * as userRepo from '../repositories/user.repository';

export async function createConnectedAccount(user: userRepo.UserRow): Promise<string> {
  const stripe = getStripe();

  const account = await stripe.accounts.create({
    type: 'express',
    email: user.email,
    metadata: {
      sportsbooks_user_id: String(user.id),
    },
  });

  await userRepo.updateStripeAccount(user.id, {
    stripeAccountId: account.id,
    stripeOnboardingStatus: 'pending',
  });

  return account.id;
}

export async function createOnboardingLink(stripeAccountId: string): Promise<string> {
  const stripe = getStripe();

  const accountLink = await stripe.accountLinks.create({
    account: stripeAccountId,
    refresh_url: env.stripeConnectRefreshUrl,
    return_url: env.stripeConnectReturnUrl,
    type: 'account_onboarding',
  });

  return accountLink.url;
}

export async function createDashboardLink(stripeAccountId: string): Promise<string> {
  const stripe = getStripe();
  const loginLink = await stripe.accounts.createLoginLink(stripeAccountId);
  return loginLink.url;
}

export async function getAccountStatus(stripeAccountId: string): Promise<{
  detailsSubmitted: boolean;
  chargesEnabled: boolean;
  payoutsEnabled: boolean;
}> {
  const stripe = getStripe();
  const account = await stripe.accounts.retrieve(stripeAccountId);

  return {
    detailsSubmitted: account.details_submitted ?? false,
    chargesEnabled: account.charges_enabled ?? false,
    payoutsEnabled: account.payouts_enabled ?? false,
  };
}

export async function handleAccountUpdated(stripeAccountId: string): Promise<void> {
  const user = await userRepo.findByStripeAccountId(stripeAccountId);
  if (!user) return;

  const status = await getAccountStatus(stripeAccountId);

  const onboardingStatus = status.detailsSubmitted && status.chargesEnabled
    ? 'complete'
    : 'pending';

  await userRepo.updateStripeAccount(user.id, {
    stripeOnboardingStatus: onboardingStatus,
    stripePayoutsEnabled: status.payoutsEnabled,
  });
}

export async function resolvePartnerStripeAccountId(
  venueId: number | null,
  coachId: number | null
): Promise<string | null> {
  const { query } = await import('../config/database');

  let ownerId: number | null = null;

  if (venueId) {
    const result = await query('SELECT owner_id FROM venues WHERE id = $1', [venueId]);
    ownerId = result.rows[0]?.owner_id ?? null;
  } else if (coachId) {
    const result = await query('SELECT user_id FROM coaches WHERE id = $1', [coachId]);
    ownerId = result.rows[0]?.user_id ?? null;
  }

  if (!ownerId) return null;

  const partner = await userRepo.findById(ownerId);
  if (!partner?.stripeAccountId || !partner.stripePayoutsEnabled) return null;

  return partner.stripeAccountId;
}
