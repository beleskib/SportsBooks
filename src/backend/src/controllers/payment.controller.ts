import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as stripeService from '../services/stripe.service';
import * as paymentRepo from '../repositories/payment.repository';
import * as bookingRepo from '../repositories/booking.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { NotFoundError, ValidationError, ForbiddenError } from '../utils/errors';
import * as userRepo from '../repositories/user.repository';
import { syncBookingToFirestore } from '../services/firestoreBookingSync.service';

/**
 * Check whether the requesting user is the payer, the partner receiving
 * payment (venue owner or coach), or an admin.
 */
async function verifyPaymentAccess(
  userId: number,
  userRole: string,
  payment: paymentRepo.PaymentRow,
): Promise<boolean> {
  if (userRole === 'admin') return true;
  if (Number(payment.payerId) === userId) return true;

  // Look up the associated booking to find the partner
  const booking = await bookingRepo.findById(payment.bookingId);
  if (booking) {
    if (booking.venueId) {
      const venue = await venueRepo.findById(booking.venueId);
      if (venue && Number(venue.ownerId) === userId) return true;
    }
    if (booking.coachId) {
      const coach = await coachRepo.findById(booking.coachId);
      if (coach && Number(coach.userId) === userId) return true;
    }
  }
  return false;
}

async function resolveUserId(req: Request): Promise<number> {
  if (req.user?.id) return req.user.id;
  const firebaseUid = (req as any).firebaseUid;
  if (firebaseUid) {
    const user = await userRepo.findByFirebaseUid(firebaseUid);
    if (user) return user.id;
  }
  throw new ValidationError('Unable to resolve user');
}

export async function createPaymentIntent(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const { bookingId } = req.body;

    if (!bookingId) {
      throw new ValidationError('bookingId is required');
    }

    const result = await stripeService.createPaymentIntent(userId, bookingId);
    created(res, result, 'Payment intent created');
  } catch (e) {
    next(e);
  }
}

export async function confirmPayment(req: Request, res: Response, next: NextFunction) {
  try {
    const paymentId = Number(req.params.id);

    // Look up the payment record so we can check ownership and dev status
    const payment = await paymentRepo.findById(paymentId);
    if (!payment) throw new NotFoundError('Payment');

    const isAdmin = req.user?.role === 'admin';
    const isDevPayment = payment.externalPaymentId?.startsWith('dev_') ?? false;

    if (!isAdmin) {
      // Allow the payer to confirm their own dev-mode payments
      const userId = await resolveUserId(req);
      const isPayer = Number(payment.payerId) === userId;

      if (!(isPayer && isDevPayment)) {
        throw new ForbiddenError('Forbidden');
      }
    }

    const confirmed = await stripeService.confirmPayment(paymentId);
    success(res, confirmed, 'Payment confirmed');
  } catch (e) {
    next(e);
  }
}

export async function failPayment(req: Request, res: Response, next: NextFunction) {
  try {
    const paymentId = Number(req.params.id);

    // Look up the payment record so we can check ownership and dev status
    const payment = await paymentRepo.findById(paymentId);
    if (!payment) throw new NotFoundError('Payment');

    const isAdmin = req.user?.role === 'admin';
    const isDevPayment = payment.externalPaymentId?.startsWith('dev_') ?? false;

    if (!isAdmin) {
      // Allow the payer to fail their own dev-mode payments
      const userId = await resolveUserId(req);
      const isPayer = Number(payment.payerId) === userId;

      if (!(isPayer && isDevPayment)) {
        throw new ForbiddenError('Forbidden');
      }
    }

    const failed = await stripeService.failPayment(paymentId);
    success(res, failed, 'Payment marked as failed');
  } catch (e) {
    next(e);
  }
}

export async function getMyPayments(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const payments = await paymentRepo.findByPayerId(userId);
    success(res, payments);
  } catch (e) {
    next(e);
  }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const payment = await paymentRepo.findById(Number(req.params.id));
    if (!payment) throw new NotFoundError('Payment');

    const userId = await resolveUserId(req);
    const userRole = req.user?.role ?? '';
    const authorized = await verifyPaymentAccess(userId, userRole, payment);
    if (!authorized) {
      throw new ForbiddenError('Forbidden');
    }

    success(res, payment);
  } catch (e) {
    next(e);
  }
}

export async function getByBookingId(req: Request, res: Response, next: NextFunction) {
  try {
    const payment = await paymentRepo.findByBookingId(Number(req.params.bookingId));
    if (!payment) throw new NotFoundError('Payment');

    const userId = await resolveUserId(req);
    const userRole = req.user?.role ?? '';
    const authorized = await verifyPaymentAccess(userId, userRole, payment);
    if (!authorized) {
      throw new ForbiddenError('Forbidden');
    }

    success(res, payment);
  } catch (e) {
    next(e);
  }
}

export async function cashConfirm(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const { bookingId } = req.body;

    if (!bookingId) {
      throw new ValidationError('bookingId is required');
    }

    // Validate booking exists, belongs to user, and is in approved status
    const booking = await bookingRepo.findById(bookingId);
    if (!booking) throw new NotFoundError('Booking');

    if (booking.playerId !== userId) {
      throw new ForbiddenError('You can only pay for your own bookings');
    }

    if (booking.status !== 'approved') {
      throw new ValidationError('Booking must be in approved status before payment');
    }

    // Create payment record for cash at venue
    const payment = await paymentRepo.create(
      booking.id,
      userId,
      booking.totalPrice,
      'MKD',
      'cash_at_venue',
      0
    );

    // Mark payment as pending_cash (will be completed when cash is collected)
    await paymentRepo.updateStatus(payment.id, 'pending_cash');

    // Update booking status to confirmed
    await bookingRepo.updateStatus(booking.id, 'confirmed');

    // Sync to Firestore (fire-and-forget)
    syncBookingToFirestore(booking.id).catch(() => {});
    stripeService.syncPaymentToFirestore(booking.id, payment.id).catch((err) =>
      console.error('Firestore payment sync failed:', err)
    );

    const updatedPayment = await paymentRepo.findById(payment.id);
    created(res, updatedPayment, 'Cash at venue payment confirmed');
  } catch (e) {
    next(e);
  }
}
