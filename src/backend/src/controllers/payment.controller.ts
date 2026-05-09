import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as stripeService from '../services/stripe.service';
import * as paymentRepo from '../repositories/payment.repository';
import * as bookingRepo from '../repositories/booking.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { NotFoundError, ValidationError, ForbiddenError } from '../utils/errors';
import * as userRepo from '../repositories/user.repository';

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
    // Only admins may manually confirm payments.
    // Normal flow is via the Stripe webhook (webhook.controller.ts).
    if (req.user?.role !== 'admin') {
      throw new ForbiddenError('Forbidden');
    }
    const payment = await stripeService.confirmPayment(Number(req.params.id));
    success(res, payment, 'Payment confirmed');
  } catch (e) {
    next(e);
  }
}

export async function failPayment(req: Request, res: Response, next: NextFunction) {
  try {
    // Only admins may manually mark a payment as failed.
    // Normal flow is via the Stripe webhook (webhook.controller.ts).
    if (req.user?.role !== 'admin') {
      throw new ForbiddenError('Forbidden');
    }
    const payment = await stripeService.failPayment(Number(req.params.id));
    success(res, payment, 'Payment marked as failed');
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
