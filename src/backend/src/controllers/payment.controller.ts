import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as stripeService from '../services/stripe.service';
import * as paymentRepo from '../repositories/payment.repository';
import { NotFoundError, ValidationError } from '../utils/errors';
import * as userRepo from '../repositories/user.repository';

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
    const payment = await stripeService.confirmPayment(Number(req.params.id));
    success(res, payment, 'Payment confirmed');
  } catch (e) {
    next(e);
  }
}

export async function failPayment(req: Request, res: Response, next: NextFunction) {
  try {
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
    success(res, payment);
  } catch (e) {
    next(e);
  }
}

export async function getByBookingId(req: Request, res: Response, next: NextFunction) {
  try {
    const payment = await paymentRepo.findByBookingId(Number(req.params.bookingId));
    if (!payment) throw new NotFoundError('Payment');
    success(res, payment);
  } catch (e) {
    next(e);
  }
}
