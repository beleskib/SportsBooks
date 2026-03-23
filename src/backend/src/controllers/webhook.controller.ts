import { Request, Response, NextFunction } from 'express';
import Stripe from 'stripe';
import { getStripe } from '../config/stripe';
import { env } from '../config/env';
import * as paymentRepo from '../repositories/payment.repository';
import * as bookingRepo from '../repositories/booking.repository';
import { query } from '../config/database';
import * as stripeConnectService from '../services/stripeConnect.service';

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
        }
        break;
      }
      case 'payment_intent.payment_failed': {
        const pi = event.data.object as Stripe.PaymentIntent;
        const payment = await paymentRepo.findByExternalId(pi.id);
        if (payment && payment.status === 'pending') {
          await paymentRepo.updateStatus(payment.id, 'failed');
          await bookingRepo.updateStatus(payment.bookingId, 'cancelled');
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
    }

    res.json({ received: true });
  } catch (e) {
    next(e);
  }
}
