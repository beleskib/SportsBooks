import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { query } from '../config/database';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import * as participantRepo from '../repositories/bookingParticipant.repository';
import * as notificationService from '../services/notification.service';

// ============================================================
// v2-practical-ux: Booking Participants
// POST /api/bookings/:id/invite      -> invite friends
// POST /api/bookings/:id/respond     -> accept/decline invite
// POST /api/bookings/:id/attendance  -> mark no-show / attended
// ============================================================

export async function inviteParticipants(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const { userIds } = req.body as { userIds?: number[] };

    if (!userIds || userIds.length === 0) {
      throw new ValidationError('userIds required');
    }

    const bookingRes = await query(
      `SELECT player_id FROM bookings WHERE id = $1`,
      [bookingId],
    );
    if (bookingRes.rows.length === 0) throw new NotFoundError('Booking');
    if (Number(bookingRes.rows[0].player_id) !== req.user!.id) {
      throw new ForbiddenError('Only the booker can invite participants');
    }

    const participants = await participantRepo.inviteMany(bookingId, userIds);

    // Notify each invitee
    for (const p of participants) {
      notificationService
        .sendNotification(
          p.userId,
          'booking_invite',
          'You were invited to a booking',
          'Tap to accept or decline',
          { bookingId },
        )
        .catch(() => {});
    }

    created(res, participants, `Invited ${participants.length} player(s)`);
  } catch (e) {
    next(e);
  }
}

export async function respondToInvite(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const { accept } = req.body as { accept?: boolean };

    if (typeof accept !== 'boolean') {
      throw new ValidationError('accept (boolean) required');
    }

    const updated = await participantRepo.respond(bookingId, req.user!.id, accept);
    if (!updated) throw new NotFoundError('Booking invite');

    success(res, updated, accept ? 'Invite accepted' : 'Invite declined');
  } catch (e) {
    next(e);
  }
}

export async function markAttendance(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const { attendance } = req.body as {
      attendance?: Array<{ userId: number; attended: boolean }>;
    };

    if (!attendance || attendance.length === 0) {
      throw new ValidationError('attendance array required');
    }

    // Only the booker, a venue owner, or an admin can mark attendance
    const bookingRes = await query(
      `SELECT b.player_id, v.owner_id AS venue_owner_id
       FROM bookings b
       LEFT JOIN venues v ON v.id = b.venue_id
       WHERE b.id = $1`,
      [bookingId],
    );
    if (bookingRes.rows.length === 0) throw new NotFoundError('Booking');

    const isBooker = Number(bookingRes.rows[0].player_id) === req.user!.id;
    const isVenueOwner =
      bookingRes.rows[0].venue_owner_id && Number(bookingRes.rows[0].venue_owner_id) === req.user!.id;

    if (!isBooker && !isVenueOwner) {
      throw new ForbiddenError('Only the booker or venue owner can mark attendance');
    }

    const updated = await participantRepo.markAttendance(bookingId, attendance);
    success(res, updated, 'Attendance marked');
  } catch (e) {
    next(e);
  }
}

export async function listParticipants(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const participants = await participantRepo.findByBookingId(bookingId);
    success(res, participants);
  } catch (e) {
    next(e);
  }
}
