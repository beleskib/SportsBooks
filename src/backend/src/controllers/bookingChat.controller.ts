import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import * as bookingRepo from '../repositories/booking.repository';
import * as bookingChatRepo from '../repositories/bookingChat.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { query } from '../config/database';

// ── Helpers ─────────────────────────────────────────────────────────

/**
 * Returns the partner user ID for a booking (venue owner or coach user).
 * Returns null if neither venue nor coach is found.
 */
async function getPartnerUserId(booking: bookingRepo.BookingRow): Promise<number | null> {
  if (booking.venueId) {
    const venue = await venueRepo.findById(booking.venueId);
    if (venue) return venue.ownerId;
  }
  if (booking.coachId) {
    const coach = await coachRepo.findById(booking.coachId);
    if (coach) return coach.userId;
  }
  return null;
}

/**
 * Verifies that the requesting user is either the player or the partner
 * associated with the booking. Returns the booking and partner user ID.
 */
async function verifyBookingParticipant(
  bookingId: number,
  userId: number
): Promise<{ booking: bookingRepo.BookingRow; partnerUserId: number | null }> {
  const booking = await bookingRepo.findById(bookingId);
  if (!booking) throw new NotFoundError('Booking');

  const partnerUserId = await getPartnerUserId(booking);
  const isPlayer = booking.playerId === userId;
  const isPartner = partnerUserId !== null && partnerUserId === userId;

  if (!isPlayer && !isPartner) {
    throw new ForbiddenError('You are not a participant in this booking');
  }

  return { booking, partnerUserId };
}

const CHAT_ALLOWED_STATUSES = ['approved', 'confirmed', 'completed'];

// ── Controller functions ────────────────────────────────────────────

export async function getMessages(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const userId = req.user!.id;

    const { booking } = await verifyBookingParticipant(bookingId, userId);

    if (!CHAT_ALLOWED_STATUSES.includes(booking.status)) {
      throw new ValidationError(
        'Chat is only available for approved, confirmed, or completed bookings'
      );
    }

    const messages = await bookingChatRepo.getMessages(bookingId);
    success(res, messages);
  } catch (e) { next(e); }
}

export async function sendMessage(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const userId = req.user!.id;
    const { message } = req.body;

    if (!message || typeof message !== 'string' || message.trim().length === 0) {
      throw new ValidationError('Message text is required');
    }

    const { booking } = await verifyBookingParticipant(bookingId, userId);

    if (!CHAT_ALLOWED_STATUSES.includes(booking.status)) {
      throw new ValidationError(
        'Chat is only available for approved, confirmed, or completed bookings'
      );
    }

    const newMessage = await bookingChatRepo.sendMessage(bookingId, userId, message.trim());
    created(res, newMessage, 'Message sent');
  } catch (e) { next(e); }
}

export async function getContactInfo(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const userId = req.user!.id;

    const { booking, partnerUserId } = await verifyBookingParticipant(bookingId, userId);

    if (!CHAT_ALLOWED_STATUSES.includes(booking.status)) {
      throw new ValidationError(
        'Contact info is only available for approved, confirmed, or completed bookings'
      );
    }

    // Determine who the "other party" is
    const isPlayer = booking.playerId === userId;
    const otherUserId = isPlayer ? partnerUserId : booking.playerId;

    if (!otherUserId) {
      throw new NotFoundError('Contact info for the other party');
    }

    // Fetch the other party's contact details
    const result = await query(
      `SELECT id, display_name, email, phone_number
       FROM users
       WHERE id = $1`,
      [otherUserId]
    );

    if (result.rows.length === 0) {
      throw new NotFoundError('User');
    }

    const row = result.rows[0];
    success(res, {
      userId: row.id,
      name: row.display_name,
      email: row.email,
      phone: row.phone_number ?? null,
    });
  } catch (e) { next(e); }
}
