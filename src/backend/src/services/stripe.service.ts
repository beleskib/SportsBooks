import { getStripe } from '../config/stripe';
import * as bookingRepo from '../repositories/booking.repository';
import * as paymentRepo from '../repositories/payment.repository';
import * as gamificationRepo from '../repositories/gamification.repository';
import { query } from '../config/database';
import { NotFoundError, ValidationError } from '../utils/errors';
import { resolvePartnerStripeAccountId } from './stripeConnect.service';
import { getFirestoreDb } from '../config/firebase';
import { syncBookingToFirestore } from './firestoreBookingSync.service';

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

  // 4. Dev mode bypass: skip Stripe when no valid key is configured
  if (process.env.DEV_AUTH_BYPASS === 'true' && process.env.NODE_ENV !== 'production') {
    const devPaymentId = `dev_pi_${Date.now()}_${booking.id}`;
    const platformFee = Math.round(effectivePrice * PLATFORM_FEE_PERCENT);
    const payment = await paymentRepo.create(
      booking.id,
      playerId,
      effectivePrice,
      'MKD',
      devPaymentId,
      platformFee
    );

    return {
      clientSecret: `dev_secret_${devPaymentId}`,
      bookingId: booking.id,
      paymentId: payment.id,
      amount: effectivePrice,
      currency: 'MKD',
    };
  }

  // 5. Resolve partner's Stripe Connect account for destination charges
  const stripe = getStripe();
  const amountInCents = Math.round(effectivePrice * 100);
  const partnerStripeAccountId = await resolvePartnerStripeAccountId(
    booking.venueId ?? null,
    booking.coachId ?? null
  );

  // 6. Create Stripe PaymentIntent (with destination charge if partner is onboarded)
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

  const paymentIntent = await stripe.paymentIntents.create(paymentIntentParams as any);

  // 7. Create payment record (pending)
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

  // Verify with Stripe that payment succeeded (skip in dev mode)
  if (payment.externalPaymentId && !payment.externalPaymentId.startsWith('dev_')) {
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

  // Sync to Firestore payments collection
  syncPaymentToFirestore(payment.bookingId, paymentId).catch((err) =>
    console.error('Firestore payment sync failed:', err)
  );

  // Sync booking status to Firestore bookings collection
  syncBookingToFirestore(payment.bookingId).catch(() => {});

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

  // Sync cancellation to Firestore bookings collection
  syncBookingToFirestore(payment.bookingId).catch(() => {});

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

export async function syncPaymentToFirestore(bookingId: number, paymentId: number): Promise<void> {
  const db = getFirestoreDb();
  if (!db) return;

  const result = await query(
    `SELECT
      b.id AS booking_id, b.total_price, b.notes,
      p.id AS payment_id, p.amount, p.currency, p.status, p.payment_method,
      p.external_payment_id, p.platform_fee_amount, p.paid_at,
      ts.slot_date, ts.start_time, ts.end_time,
      v.name AS venue_name, v.address AS venue_address, v.sport_type AS venue_sport,
      c.name AS coach_name, c.address AS coach_address, c.sport_type AS coach_sport,
      u.display_name AS player_name, u.email AS player_email, u.firebase_uid
    FROM bookings b
    JOIN payments p ON p.booking_id = b.id
    LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    JOIN users u ON u.id = b.player_id
    WHERE b.id = $1 AND p.id = $2`,
    [bookingId, paymentId]
  );

  if (result.rows.length === 0) return;
  const row = result.rows[0];

  await db.collection('payments').doc(String(row.payment_id)).set({
    paymentId: Number(row.payment_id),
    bookingId: Number(row.booking_id),
    receiptNumber: `SB-${row.booking_id}-${row.payment_id}`,
    playerName: row.player_name,
    playerEmail: row.player_email,
    playerFirebaseUid: row.firebase_uid,
    providerType: row.venue_name ? 'venue' : 'coach',
    providerName: row.venue_name || row.coach_name,
    providerAddress: row.venue_address || row.coach_address || null,
    sportType: row.venue_sport || row.coach_sport,
    slotDate: row.slot_date instanceof Date ? row.slot_date.toISOString().split('T')[0] : row.slot_date,
    startTime: row.start_time,
    endTime: row.end_time,
    amount: Number(row.amount),
    currency: row.currency,
    platformFee: row.platform_fee_amount ? Number(row.platform_fee_amount) : 0,
    total: Number(row.total_price),
    status: row.status,
    paymentMethod: row.payment_method,
    externalPaymentId: row.external_payment_id,
    paidAt: row.paid_at,
    createdAt: new Date().toISOString(),
  });
}
