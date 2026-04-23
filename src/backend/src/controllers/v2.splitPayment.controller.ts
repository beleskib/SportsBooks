import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { query } from '../config/database';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import * as splitRepo from '../repositories/splitPayment.repository';
import * as participantRepo from '../repositories/bookingParticipant.repository';
import * as notificationService from '../services/notification.service';

// ============================================================
// v2-practical-ux: Split Payment controller
// POST /api/bookings/:id/split   -> create shares
// GET  /api/bookings/:id/split   -> summary
// POST /api/split-payments/:shareId/pay -> mark share paid
// ============================================================

export async function createSplit(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const { payerUserIds, customAmounts, expiresInMinutes } = req.body as {
      payerUserIds?: number[];
      customAmounts?: Array<{ userId: number; amount: number }>;
      expiresInMinutes?: number;
    };

    if ((!payerUserIds || payerUserIds.length === 0) && !customAmounts) {
      throw new ValidationError('payerUserIds or customAmounts required');
    }

    // Verify caller owns the booking
    const bookingRes = await query(
      `SELECT id, player_id, total_price FROM bookings WHERE id = $1`,
      [bookingId],
    );
    if (bookingRes.rows.length === 0) throw new NotFoundError('Booking');
    if (Number(bookingRes.rows[0].player_id) !== req.user!.id) {
      throw new ForbiddenError('Only the booker can split this payment');
    }

    const totalPrice = Number(bookingRes.rows[0].total_price);
    const expiresAt = expiresInMinutes
      ? new Date(Date.now() + expiresInMinutes * 60 * 1000)
      : null;

    // Compute shares
    let shares: Array<{ userId: number; amount: number }> = [];

    if (customAmounts && customAmounts.length > 0) {
      const sum = customAmounts.reduce((s, c) => s + c.amount, 0);
      // Allow a 1-cent rounding drift
      if (Math.abs(sum - totalPrice) > 0.01) {
        throw new ValidationError(`Custom amounts must sum to ${totalPrice}, got ${sum}`);
      }
      shares = customAmounts;
    } else {
      // Equal split across booker + invited payers
      const payers = [req.user!.id, ...(payerUserIds ?? [])];
      const share = Math.round((totalPrice / payers.length) * 100) / 100;
      shares = payers.map(userId => ({ userId, amount: share }));
      // Correct last share for rounding
      const drift = totalPrice - shares.reduce((s, c) => s + c.amount, 0);
      if (Math.abs(drift) > 0.001 && shares.length > 0) {
        shares[shares.length - 1].amount = Math.round((shares[shares.length - 1].amount + drift) * 100) / 100;
      }
    }

    // Create all shares
    const createdShares = [];
    for (const s of shares) {
      const share = await splitRepo.createShare(bookingId, s.userId, s.amount, 'EUR', expiresAt);
      createdShares.push(share);

      // Link to booking_participants if exists (auto-invite as a participant if not yet)
      await participantRepo.inviteMany(bookingId, [s.userId]).catch(() => {});
      await participantRepo.linkSplitPayment(bookingId, s.userId, share.id);

      // Notify payer (skip the booker themselves)
      if (s.userId !== req.user!.id) {
        notificationService
          .sendNotification(
            s.userId,
            'split_payment_request',
            'You have a payment share',
            `Pay your share of €${s.amount.toFixed(2)} for the booking`,
            { bookingId, shareId: share.id },
          )
          .catch(() => {});
      }
    }

    const summary = await splitRepo.getSummary(bookingId);
    created(
      res,
      {
        bookingId,
        ...summary,
        shares: createdShares,
      },
      'Split payment created',
    );
  } catch (e) {
    next(e);
  }
}

export async function getSplitSummary(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);

    const bookingRes = await query(
      `SELECT player_id FROM bookings WHERE id = $1`,
      [bookingId],
    );
    if (bookingRes.rows.length === 0) throw new NotFoundError('Booking');

    const shares = await splitRepo.findByBookingId(bookingId);
    const summary = await splitRepo.getSummary(bookingId);

    // Only booker + payers can see this
    const isBooker = Number(bookingRes.rows[0].player_id) === req.user!.id;
    const isPayer = shares.some(s => s.payerUserId === req.user!.id);
    if (!isBooker && !isPayer) throw new ForbiddenError('Not authorized');

    success(res, { bookingId, ...summary, shares });
  } catch (e) {
    next(e);
  }
}

export async function payShare(req: Request, res: Response, next: NextFunction) {
  try {
    const shareId = Number(req.params.shareId);
    const { stripePaymentIntentId } = req.body as { stripePaymentIntentId?: string };

    if (!stripePaymentIntentId) {
      throw new ValidationError('stripePaymentIntentId required');
    }

    const share = await splitRepo.findById(shareId);
    if (!share) throw new NotFoundError('Split payment share');
    if (share.payerUserId !== req.user!.id) {
      throw new ForbiddenError('You can only pay your own share');
    }
    if (share.status === 'paid') {
      return success(res, share, 'Share already paid');
    }

    const updated = await splitRepo.markPaid(shareId, stripePaymentIntentId);
    success(res, updated, 'Share marked paid');
  } catch (e) {
    next(e);
  }
}
