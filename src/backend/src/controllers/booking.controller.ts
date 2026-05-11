import { Request, Response, NextFunction } from 'express';
import { success, created, paginated } from '../utils/apiResponse';
import * as bookingRepo from '../repositories/booking.repository';
import * as gamificationRepo from '../repositories/gamification.repository';
import * as notificationRepo from '../repositories/notification.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import * as venueBookingLobbyRepo from '../repositories/venueBookingLobby.repository';
import * as timeSlotRepo from '../repositories/timeSlot.repository';
import { sendBookingConfirmedEmails } from '../services/bookingEmail.service';
import { sendPartnerBookingRequestEmail } from '../services/partnerApproval.service';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import { query } from '../config/database';
import { syncBookingToFirestore, syncBookingsToFirestore } from '../services/firestoreBookingSync.service';

/**
 * Auto-complete past confirmed/approved bookings.
 * When a booking's time slot date + end time is in the past and the booking
 * is still confirmed or approved, we mark it as 'completed' and award XP.
 * Called lazily when bookings are fetched — no cron job required.
 */
async function autoCompletePastBookings(userId?: number): Promise<void> {
  try {
    const result = await query(
      `UPDATE bookings b
       SET status = 'completed', updated_at = NOW()
       FROM time_slots ts
       WHERE ts.id = b.time_slot_id
         AND b.status IN ('confirmed', 'approved')
         AND (ts.slot_date + ts.end_time) < NOW()
         ${userId ? 'AND b.player_id = $1' : ''}
       RETURNING b.id, b.player_id`,
      userId ? [userId] : []
    );

    // Award XP for each auto-completed booking (fire-and-forget)
    for (const row of result.rows) {
      gamificationRepo.addXpTransaction(
        Number(row.player_id),
        25,
        'booking_completed',
        Number(row.id),
        `Auto-completed booking #${row.id}`
      ).catch(() => {});
    }

    // Sync all auto-completed bookings to Firestore
    if (result.rows.length > 0) {
      syncBookingsToFirestore(result.rows.map((r: any) => Number(r.id))).catch(() => {});
    }
  } catch (e) {
    console.error('Auto-complete past bookings failed:', e);
  }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const { timeSlotId, notes } = req.body;
    const booking = await bookingRepo.create(req.user!.id, timeSlotId, notes);

    // Send notification to partner about new booking request
    try {
      let partnerId: number | null = null;
      let entityName = '';
      if (booking.venueId) {
        const venue = await venueRepo.findById(booking.venueId);
        if (venue) { partnerId = venue.ownerId; entityName = venue.name; }
      } else if (booking.coachId) {
        const coach = await coachRepo.findById(booking.coachId);
        if (coach) { partnerId = coach.userId; entityName = coach.name; }
      }
      if (partnerId) {
        await notificationRepo.createNotification(
          partnerId,
          'booking_request',
          'New Booking Request',
          `You have a new booking request for ${entityName}. Tap to review.`,
          { bookingId: String(booking.id) }
        );
      }
    } catch (e) {
      console.error('Failed to send booking request notification:', e);
    }

    // Email the partner with a login-required link to the dashboard
    // (fire-and-forget; reminders at +2h/+6h handled by cron).
    sendPartnerBookingRequestEmail(booking.id).catch((e) =>
      console.error('Failed to send partner approval email:', e)
    );

    // Sync new booking to Firestore for real-time updates
    syncBookingToFirestore(booking.id).catch(() => {});

    created(res, booking, 'Booking created');
  } catch (e) { next(e); }
}

export async function getMyBookings(req: Request, res: Response, next: NextFunction) {
  try {
    // Auto-complete any past confirmed bookings before fetching
    await autoCompletePastBookings(req.user!.id);

    const status = req.query.status ? String(req.query.status) : undefined;
    const page = req.query.page ? Number(req.query.page) : 1;
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const result = await bookingRepo.findByPlayerId(req.user!.id, status, { page, limit });
    paginated(res, result.data, { page: result.page, limit: result.limit, total: result.total });
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    // Auto-complete if this booking's date has passed
    await autoCompletePastBookings();

    const booking = await bookingRepo.findById(Number(req.params.id));
    if (!booking) throw new NotFoundError('Booking');

    const userId = req.user!.id;
    const userRole = req.user!.role;

    // Only the player who made the booking, the partner who owns the
    // venue/coach, or an admin may view a specific booking.
    let authorized = false;
    if (userRole === 'admin') {
      authorized = true;
    } else if (Number(booking.playerId) === userId) {
      authorized = true;
    } else {
      if (booking.venueId) {
        const venue = await venueRepo.findById(booking.venueId);
        if (venue && Number(venue.ownerId) === userId) authorized = true;
      }
      if (!authorized && booking.coachId) {
        const coach = await coachRepo.findById(booking.coachId);
        if (coach && Number(coach.userId) === userId) authorized = true;
      }
    }

    if (!authorized) {
      throw new ForbiddenError('Forbidden');
    }

    success(res, booking);
  } catch (e) { next(e); }
}

export async function updateStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const { status } = req.body;
    const userId = req.user!.id;
    const userRole = req.user!.role;

    // Validate status against allowed values
    const ALLOWED_STATUSES = ['pending', 'approved', 'confirmed', 'completed', 'cancelled'];
    if (!status || !ALLOWED_STATUSES.includes(status)) {
      throw new ValidationError(`Invalid status. Allowed values: ${ALLOWED_STATUSES.join(', ')}`);
    }

    // Fetch the existing booking and verify ownership
    const existing = await bookingRepo.findById(Number(req.params.id));
    if (!existing) throw new NotFoundError('Booking');

    // Check if user is the player, the partner, or an admin
    let isOwnerOrPlayer = false;
    if (userRole === 'admin') {
      isOwnerOrPlayer = true;
    } else if (Number(existing.playerId) === userId) {
      isOwnerOrPlayer = true;
    } else {
      // Check partner ownership via venue or coach
      if (existing.venueId) {
        const venue = await venueRepo.findById(existing.venueId);
        if (venue && Number(venue.ownerId) === userId) isOwnerOrPlayer = true;
      }
      if (!isOwnerOrPlayer && existing.coachId) {
        const coach = await coachRepo.findById(existing.coachId);
        if (coach && Number(coach.userId) === userId) isOwnerOrPlayer = true;
      }
    }

    if (!isOwnerOrPlayer) {
      throw new ForbiddenError('Forbidden');
    }

    const booking = await bookingRepo.updateStatus(Number(req.params.id), status);

    // Award XP when a booking is completed
    if (status === 'completed') {
      try {
        await gamificationRepo.addXpTransaction(
          booking.playerId, 25, 'booking_completed', booking.id, 'Completed a booking'
        );
        await gamificationRepo.updatePlayerLevel(booking.playerId);
      } catch (_xpErr) {
        // XP award failure should not break the booking update
        console.error('Failed to award XP for completed booking:', _xpErr);
      }
    }

    // Sync status change to Firestore
    syncBookingToFirestore(booking.id).catch(() => {});

    success(res, booking);
  } catch (e) { next(e); }
}

export async function getBookingCounts(_req: Request, res: Response, next: NextFunction) {
  try {
    const [venueCounts, coachCounts] = await Promise.all([
      bookingRepo.getVenueBookingCounts(),
      bookingRepo.getCoachBookingCounts(),
    ]);
    success(res, { venues: venueCounts, coaches: coachCounts });
  } catch (e) { next(e); }
}

export async function getPartnerBookings(req: Request, res: Response, next: NextFunction) {
  try {
    const status = req.query.status ? String(req.query.status) : undefined;
    const page = req.query.page ? Number(req.query.page) : 1;
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const result = await bookingRepo.findByPartner(req.user!.id, status, { page, limit });
    paginated(res, result.data, { page: result.page, limit: result.limit, total: result.total });
  } catch (e) { next(e); }
}

export const approveBooking = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;
    const userId = req.user!.id;

    // Verify the booking exists and belongs to this partner
    const existing = await bookingRepo.findById(Number(id));
    if (!existing) throw new NotFoundError('Booking');

    // Verify partner ownership
    let isOwner = false;
    let entityName = '';
    if (existing.venueId) {
      const venue = await venueRepo.findById(existing.venueId);
      if (venue && Number(venue.ownerId) === userId) { isOwner = true; entityName = venue.name; }
    }
    if (existing.coachId) {
      const coach = await coachRepo.findById(existing.coachId);
      if (coach && Number(coach.userId) === userId) { isOwner = true; entityName = coach.name; }
    }
    if (!isOwner) throw new ForbiddenError('You can only approve bookings for your own venues/coaches');

    const booking = await bookingRepo.approveBooking(Number(id));

    // Close the time slot so no one else can book it
    if (existing.timeSlotId) {
      try {
        await timeSlotRepo.setAvailability(existing.timeSlotId, false);
      } catch (e) {
        console.error('Failed to close time slot on approval:', e);
      }
    }

    // Notify the player
    try {
      await notificationRepo.createNotification(
        booking.playerId,
        'booking_approved',
        'Booking Approved!',
        `Your booking at "${entityName}" has been approved. Proceed to payment.`,
        { bookingId: String(booking.id) }
      );
    } catch (e) {
      console.error('Failed to send booking approved notification:', e);
    }

    // Email receipt to both player + partner (fire-and-forget, never blocks)
    sendBookingConfirmedEmails(booking.id).catch((e) =>
      console.error('Failed to send booking confirmation emails:', e)
    );

    // Sync approval to Firestore
    syncBookingToFirestore(booking.id).catch(() => {});

    // Check if this booking is linked to a venue booking lobby
    try {
      const lobbyForBooking = await venueBookingLobbyRepo.findByBookingId(booking.id);
      if (lobbyForBooking) {
        await venueBookingLobbyRepo.updateStatus(lobbyForBooking.id, 'booking_approved');
        // Update all joined members to payment_pending
        const members = await venueBookingLobbyRepo.getMembers(lobbyForBooking.id);
        for (const member of members) {
          if (member.status === 'joined') {
            await venueBookingLobbyRepo.updateMemberPaymentStatus(
              lobbyForBooking.id, member.userId, null, 'payment_pending'
            );
          }
        }
      }
    } catch (e) {
      console.error('Failed to update lobby status on booking approval:', e);
    }

    success(res, booking);
  } catch (error) {
    next(error);
  }
};

export const declineBooking = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;
    const userId = req.user!.id;

    const existing = await bookingRepo.findById(Number(id));
    if (!existing) throw new NotFoundError('Booking');

    let isOwner = false;
    let entityName = '';
    if (existing.venueId) {
      const venue = await venueRepo.findById(existing.venueId);
      if (venue && Number(venue.ownerId) === userId) { isOwner = true; entityName = venue.name; }
    }
    if (existing.coachId) {
      const coach = await coachRepo.findById(existing.coachId);
      if (coach && Number(coach.userId) === userId) { isOwner = true; entityName = coach.name; }
    }
    if (!isOwner) throw new ForbiddenError('You can only decline bookings for your own venues/coaches');

    const booking = await bookingRepo.declineBooking(Number(id));

    // Notify the player
    try {
      await notificationRepo.createNotification(
        booking.playerId,
        'booking_declined',
        'Booking Declined',
        `Your booking at "${entityName}" has been declined.`,
        { bookingId: String(booking.id) }
      );
    } catch (e) {
      console.error('Failed to send booking declined notification:', e);
    }

    // Sync decline to Firestore
    syncBookingToFirestore(booking.id).catch(() => {});

    // Check if this booking is linked to a venue booking lobby and cancel it
    try {
      const lobbyForBooking = await venueBookingLobbyRepo.findByBookingId(booking.id);
      if (lobbyForBooking) {
        await venueBookingLobbyRepo.updateStatus(lobbyForBooking.id, 'cancelled');
      }
    } catch (e) {
      console.error('Failed to cancel lobby on booking decline:', e);
    }

    success(res, booking);
  } catch (error) {
    next(error);
  }
};
