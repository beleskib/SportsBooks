import { Request, Response, NextFunction } from 'express';
import { getStripe } from '../config/stripe';
import { success } from '../utils/apiResponse';
import { ValidationError, NotFoundError } from '../utils/errors';
import * as subRepo from '../repositories/subscription.repository';

// ============================================================
// SportsBooks+ Subscription Controller
// ============================================================

// GET /api/subscription — current subscription state
export async function getSubscription(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const sub = await subRepo.findByUserId(userId);
    const visibility = await subRepo.getProfileVisibility(userId);
    success(res, {
      isPlus: sub !== null,
      subscription: sub
        ? {
            id: sub.id,
            status: sub.status,
            currentPeriodEnd: sub.currentPeriodEnd,
            cancelAtPeriodEnd: sub.cancelAtPeriodEnd,
            trialEnd: sub.trialEnd,
          }
        : null,
      profileVisibility: visibility,
    });
  } catch (e) {
    next(e);
  }
}

// POST /api/subscription/checkout — create a Stripe Checkout session
export async function createCheckout(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const stripe = getStripe();

    // Get or create Stripe customer
    let stripeCustomerId = await subRepo.getStripeCustomerId(userId);
    if (!stripeCustomerId) {
      const customer = await stripe.customers.create({
        metadata: { sportsbook_user_id: String(userId) },
      });
      stripeCustomerId = customer.id;
      await subRepo.setStripeCustomerId(userId, stripeCustomerId);
    }

    // The price ID for SportsBooks+ — stored in env so it's not hardcoded
    const priceId = process.env.SPORTSBOOKS_PLUS_PRICE_ID;
    if (!priceId) {
      throw new ValidationError(
        'SportsBooks+ pricing not configured. Set SPORTSBOOKS_PLUS_PRICE_ID.',
      );
    }

    const session = await stripe.checkout.sessions.create({
      customer: stripeCustomerId,
      mode: 'subscription',
      line_items: [{ price: priceId, quantity: 1 }],
      subscription_data: {
        trial_period_days: 7,
        metadata: { sportsbook_user_id: String(userId) },
      },
      success_url:
        process.env.SPORTSBOOKS_PLUS_SUCCESS_URL ||
        'http://localhost:5173/settings?plus=success',
      cancel_url:
        process.env.SPORTSBOOKS_PLUS_CANCEL_URL ||
        'http://localhost:5173/settings?plus=cancel',
      metadata: { sportsbook_user_id: String(userId) },
    });

    success(res, { checkoutUrl: session.url, sessionId: session.id });
  } catch (e) {
    next(e);
  }
}

// POST /api/subscription/cancel — cancels at end of current period
export async function cancelSubscription(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const sub = await subRepo.findByUserId(userId);
    if (!sub) throw new NotFoundError('No active subscription');

    const stripe = getStripe();
    await stripe.subscriptions.update(sub.stripeSubscriptionId, {
      cancel_at_period_end: true,
    });

    // The webhook will sync state, but optimistically update local
    await subRepo.upsert({
      ...sub,
      cancelAtPeriodEnd: true,
    });

    success(res, { message: 'Subscription will cancel at end of billing period' });
  } catch (e) {
    next(e);
  }
}

// POST /api/subscription/reactivate — un-cancels a pending cancellation
export async function reactivateSubscription(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const sub = await subRepo.findByUserId(userId);
    if (!sub) throw new NotFoundError('No active subscription');

    const stripe = getStripe();
    await stripe.subscriptions.update(sub.stripeSubscriptionId, {
      cancel_at_period_end: false,
    });

    await subRepo.upsert({
      ...sub,
      cancelAtPeriodEnd: false,
      canceledAt: null,
    });

    success(res, { message: 'Subscription reactivated' });
  } catch (e) {
    next(e);
  }
}

// PUT /api/subscription/visibility — set profile visibility (Plus-only)
export async function setVisibility(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const { visibility } = req.body;

    if (!['public', 'friends_only', 'private'].includes(visibility)) {
      throw new ValidationError('visibility must be public, friends_only, or private');
    }

    // Non-Plus users can only be public
    const sub = await subRepo.findByUserId(userId);
    if (!sub && visibility !== 'public') {
      throw new ValidationError(
        'Profile visibility options require SportsBooks+. Upgrade to control who sees your profile.',
      );
    }

    await subRepo.setProfileVisibility(userId, visibility);
    success(res, { profileVisibility: visibility });
  } catch (e) {
    next(e);
  }
}
