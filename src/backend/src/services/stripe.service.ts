import { getStripe } from '../config/stripe';
import * as bookingRepo from '../repositories/booking.repository';
import * as paymentRepo from '../repositories/payment.repository';
import * as gamificationRepo from '../repositories/gamification.repository';
import { query } from '../config/database';
import { NotFoundError, ValidationError } from '../utils/errors';
import { resolvePartnerStripeAccountId } from './stripeConnect.service';

const PLATFORM_FEE_PERCENT = 0.10; // 10% commission

export interface PaymentIntentResult {
  clientSecret: string;
  bookingId: number;
  paymentId: number;
  amount: number;
  currency: string;
}

export async function createPaymentIntent(
  playerId: number,
  bookingId: number
): Promise<PaymentIntentResult> {
  // 1. Fetch existing booking and validate
  const booking = await bookingRepo.findById(bookingId);
  if (!booking) throw new NotFoundError('Booking');
  if (booking.status !== 'approved') {
    throw new ValidationError('Booking must be in approved status before payment');
  }
  if (booking.playerId !== playerId) {
    throw new ValidationError('You can only pay for your own bookings');
  }

  // 2. Check if the booking has any XP redemption discount
  const xpDiscount = await gamificationRepo.getXpRedemptionDiscount(bookingId);
  const effectivePrice = Math.max(booking.totalPrice - xpDiscount, 0);

  // 3. If the XP discount covers 100% of the price, skip Stripe entirely
  if (effectivePrice === 0) {
    // Create a payment record marked as completed (no Stripe needed)
    const payment = await paymentRepo.create(
      booking.id,
      playerId,
      0,
      'MKD',
      'xp_full_coverage',
      0
    );
    await paymentRepo.updateStatus(payment.id, 'completed', new Date().toISOString());
    await bookingRepo.updateStatus(booking.id, 'confirmed');

    return {
      clientSecret: '',
      bookingId: booking.id,
      paymentId: payment.id,
      amount: 0,
      currency: 'MKD',
    };
  }

  // 4. Resolve partner's Stripe Connect account for destination charges
  const stripe = getStripe();
  const amountInCents = Math.round(effectivePrice * 100);
  const partnerStripeAccountId = await resolvePartnerStripeAccountId(
    booking.venueId ?? null,
    booking.coachId ?? null
  );

  // 5. Create Stripe PaymentIntent (with destination charge if partner is onboarded)
  let platformFeeAmount: number | undefined;
  const paymentIntentParams: Record<string, any> = {
    amount: amountInCents,
    currency: 'mkd',
    metadata: {
      bookingId: String(booking.id),
      playerId: String(playerId),
      xpDiscount: String(xpDiscount),
    },
    automatic_payment_methods: { enabled: true },
  };

  if (partnerStripeAccountId) {
    const feeInCents = Math.round(amountInCents * PLATFORM_FEE_PERCENT);
    paymentIntentParams.application_fee_amount = feeInCents;
    paymentIntentParams.transfer_data = { destination: partnerStripeAccountId };
    platformFeeAmount = feeInCents / 100; // store in denar
  }

  const paymentIntent = await stripe.paymentIntents.create(paymentIntentParams);

  // 6. Create payment record (pending)
  const payment = await paymentRepo.create(
    booking.id,
    playerId,
    effectivePrice,
    'MKD',
    paymentIntent.id,
    platformFeeAmount
  );

  return {
    clientSecret: paymentIntent.client_secret!,
    bookingId: booking.id,
    paymentId: payment.id,
    amount: effectivePrice,
    currency: 'MKD',
  };
}

export async function confirmPayment(paymentId: number): Promise<paymentRepo.PaymentRow> {
  const payment = await paymentRepo.findById(paymentId);
  if (!payment) throw new NotFoundError('Payment');

  if (payment.status === 'completed') {
    return payment; // idempotent
  }

  // Verify with Stripe that payment succeeded
  if (payment.externalPaymentId) {
    const stripe = getStripe();
    const pi = await stripe.paymentIntents.retrieve(payment.externalPaymentId);
    if (pi.status !== 'succeeded') {
      throw new ValidationError('Payment has not succeeded on Stripe');
    }
  }

  // Update payment status to completed
  const updated = await paymentRepo.updateStatus(
    paymentId,
    'completed',
    new Date().toISOString()
  );

  // Update booking status to confirmed
  await bookingRepo.updateStatus(payment.bookingId, 'confirmed');

  return updated;
}

export async function failPayment(paymentId: number): Promise<paymentRepo.PaymentRow> {
  const payment = await paymentRepo.findById(paymentId);
  if (!payment) throw new NotFoundError('Payment');

  if (payment.status === 'failed') {
    return payment; // idempotent
  }

  // Update payment to failed
  const updated = await paymentRepo.updateStatus(paymentId, 'failed');

  // Cancel the booking
  await bookingRepo.updateStatus(payment.bookingId, 'cancelled');

  // Re-open the time slot
  const booking = await bookingRepo.findById(payment.bookingId);
  if (booking) {
    await query(
      'UPDATE time_slots SET is_available = true WHERE id = $1',
      [booking.timeSlotId]
    );
  }

  return updated;
}
