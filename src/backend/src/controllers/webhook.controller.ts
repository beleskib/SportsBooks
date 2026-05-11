import { Request, Response, NextFunction } from 'express';
import Stripe from 'stripe';
import { getStripe } from '../config/stripe';
import { env } from '../config/env';
import * as paymentRepo from '../repositories/payment.repository';
import * as bookingRepo from '../repositories/booking.repository';
import { query } from '../config/database';
import * as stripeConnectService from '../services/stripeConnect.service';
import * as subRepo from '../repositories/subscription.repository';
import { syncPaymentToFirestore } from '../services/stripe.service';
import { syncBookingToFirestore } from '../services/firestoreBookingSync.service';

export async function handleStripeWebhook(req: Request, res: Response, next: NextFunction) {
  try {
    const stripe = getStripe();
    const sig = req.headers['stripe-signature'] as string;

    if (!sig || !env.stripeWebhookSecret) {
      res.status(400).json({ error: 'Missing stripe signature or webhook secret' });
      return;
    }

    const event = stripe.webhooks.constructEvent(req.body, sig, env.stripeWebhookSecret);

    switch (event.type) {
      case 'payment_intent.succeeded': {
        const pi = event.data.object as Stripe.PaymentIntent;
        const payment = await paymentRepo.findByExternalId(pi.id);
        if (payment && payment.status === 'pending') {
          await paymentRepo.updateStatus(payment.id, 'completed', new Date().toISOString());
          await bookingRepo.updateStatus(payment.bookingId, 'confirmed');

          // Sync completed payment to Firestore
          syncPaymentToFirestore(payment.bookingId, payment.id).catch((err) =>
            console.error('Firestore payment sync failed:', err)
          );
          // Sync booking status to Firestore bookings collection
          syncBookingToFirestore(payment.bookingId).catch(() => {});
        }
        break;
      }
      case 'payment_intent.payment_failed': {
        const pi = event.data.object as Stripe.PaymentIntent;
        const payment = await paymentRepo.findByExternalId(pi.id);
        if (payment && payment.status === 'pending') {
          await paymentRepo.updateStatus(payment.id, 'failed');
          await bookingRepo.updateStatus(payment.bookingId, 'cancelled');
          // Sync cancellation to Firestore bookings collection
          syncBookingToFirestore(payment.bookingId).catch(() => {});
          const booking = await bookingRepo.findById(payment.bookingId);
          if (booking) {
            await query('UPDATE time_slots SET is_available = true WHERE id = $1', [booking.timeSlotId]);
          }
        }
        break;
      }
      case 'account.updated': {
        const account = event.data.object as Stripe.Account;
        await stripeConnectService.handleAccountUpdated(account.id);
        break;
      }

      // ---- SportsBooks+ subscription lifecycle ----
      case 'customer.subscription.created':
      case 'customer.subscription.updated':
      case 'customer.subscription.deleted': {
        const sub = event.data.object as Stripe.Subscription;
        await handleSubscriptionEvent(sub);
        break;
      }
    }

    res.json({ received: true });
  } catch (e) {
    next(e);
  }
}

// ============================================================
// SportsBooks+ subscription webhook helper
// ============================================================

async function handleSubscriptionEvent(sub: Stripe.Subscription): Promise<void> {
  // Resolve SportsBook user ID from subscription metadata or customer lookup
  let userId: number | null = null;

  const metaUserId = sub.metadata?.sportsbook_user_id;
  if (metaUserId) {
    userId = Number(metaUserId);
  }

  if (!userId) {
    // Fallback: look up by Stripe customer ID in our users table
    const customerId = typeof sub.customer === 'string' ? sub.customer : sub.customer?.id;
    if (customerId) {
      const result = await query(
        `SELECT id FROM users WHERE stripe_customer_id = $1 LIMIT 1`,
        [customerId],
      );
      if (result.rows.length > 0) {
        userId = Number(result.rows[0].id);
      }
    }
  }

  if (!userId) {
    console.warn('[webhook] Could not resolve user for subscription', sub.id);
    return;
  }

  // Map Stripe subscription status to our enum
  const statusMap: Record<string, string> = {
    trialing: 'trialing',
    active: 'active',
    past_due: 'past_due',
    canceled: 'canceled',
    unpaid: 'unpaid',
    incomplete: 'unpaid',
    incomplete_expired: 'canceled',
    paused: 'canceled',
  };

  const status = statusMap[sub.status] ?? 'canceled';
  const priceId = sub.items?.data?.[0]?.price?.id ?? '';

  await subRepo.upsert({
    userId,
    stripeSubscriptionId: sub.id,
    stripeCustomerId: typeof sub.customer === 'string' ? sub.customer : sub.customer?.id ?? '',
    stripePriceId: priceId,
    status,
    currentPeriodStart: new Date((sub.current_period_start ?? 0) * 1000).toISOString(),
    currentPeriodEnd: new Date((sub.current_period_end ?? 0) * 1000).toISOString(),
    cancelAtPeriodEnd: sub.cancel_at_period_end ?? false,
    canceledAt: sub.canceled_at ? new Date(sub.canceled_at * 1000).toISOString() : null,
    trialStart: sub.trial_start ? new Date(sub.trial_start * 1000).toISOString() : null,
    trialEnd: sub.trial_end ? new Date(sub.trial_end * 1000).toISOString() : null,
  });

  // Sync the denormalized is_plus flag on the users row
  await subRepo.syncUserPlusFlag(userId);
}
