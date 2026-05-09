import { Request, Response, NextFunction } from 'express';
import { created } from '../utils/apiResponse';
import { query } from '../config/database';
import { NotFoundError, ForbiddenError, ValidationError, ConflictError } from '../utils/errors';

// ============================================================
// v2-practical-ux: One-tap rebook
// POST /api/bookings/:id/rebook
// Copies a past booking into a new slot on the same venue/coach.
// ============================================================

export async function rebookFromBooking(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const { slotDate, startTime, timeSlotId } = req.body as {
      slotDate?: string;
      startTime?: string;
      timeSlotId?: number;
    };

    if (!timeSlotId && (!slotDate || !startTime)) {
      throw new ValidationError(
        'Either timeSlotId or both slotDate (YYYY-MM-DD) and startTime (HH:mm) are required'
      );
    }

    // Load source booking and verify ownership
    const srcRes = await query(
      `SELECT b.*, ts.end_time AS src_end_time, ts.start_time AS src_start_time,
              ts.venue_id AS ts_venue_id, ts.coach_id AS ts_coach_id
       FROM bookings b
       LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
       WHERE b.id = $1`,
      [bookingId],
    );
    if (srcRes.rows.length === 0) throw new NotFoundError('Booking');

    const src = srcRes.rows[0];
    if (Number(src.player_id) !== req.user!.id) {
      throw new ForbiddenError('You can only rebook your own bookings');
    }

    const venueId = src.venue_id ? Number(src.venue_id) : null;
    const coachId = src.coach_id ? Number(src.coach_id) : null;

    let newSlotId: number;

    if (timeSlotId) {
      // ── timeSlotId path: use an existing slot directly ──
      const slotRes = await query(
        `SELECT id, venue_id, coach_id, is_available FROM time_slots WHERE id = $1`,
        [timeSlotId],
      );
      if (slotRes.rows.length === 0) throw new NotFoundError('Time slot');

      const slot = slotRes.rows[0];
      const slotVenueId = slot.venue_id ? Number(slot.venue_id) : null;
      const slotCoachId = slot.coach_id ? Number(slot.coach_id) : null;

      // Verify the slot belongs to the same venue/coach as the source booking
      if (slotVenueId !== venueId || slotCoachId !== coachId) {
        throw new ValidationError(
          'The selected time slot does not belong to the same venue/coach as the original booking'
        );
      }

      if (!slot.is_available) {
        throw new ConflictError('That slot is not available');
      }

      // Check for existing bookings on it
      const existing = await query(
        `SELECT 1 FROM bookings WHERE time_slot_id = $1 AND status IN ('pending','approved','confirmed')`,
        [timeSlotId],
      );
      if (existing.rows.length > 0) {
        throw new ConflictError('That slot is already booked');
      }

      newSlotId = Number(slot.id);
    } else {
      // ── slotDate + startTime path: look up or create a matching slot ──
      const [sh, sm] = String(src.src_start_time).split(':').map(Number);
      const [eh, em] = String(src.src_end_time).split(':').map(Number);
      const durationMinutes = (eh * 60 + em) - (sh * 60 + sm);
      const [nh, nm] = startTime!.split(':').map(Number);
      const newEndMinutes = nh * 60 + nm + durationMinutes;
      const newEndTime = `${String(Math.floor(newEndMinutes / 60)).padStart(2, '0')}:${String(newEndMinutes % 60).padStart(2, '0')}`;

      const slotRes = await query(
        `SELECT id, is_available FROM time_slots
         WHERE slot_date = $1
           AND start_time = $2::TIME
           AND end_time = $3::TIME
           AND venue_id IS NOT DISTINCT FROM $4
           AND coach_id IS NOT DISTINCT FROM $5`,
        [slotDate, startTime, newEndTime, venueId, coachId],
      );

      if (slotRes.rows.length === 0) {
        // Slot doesn't exist yet — generate one on the fly
        const gen = await query(
          `INSERT INTO time_slots (slot_date, start_time, end_time, venue_id, coach_id, is_available)
           VALUES ($1, $2::TIME, $3::TIME, $4, $5, true)
           RETURNING id`,
          [slotDate, startTime, newEndTime, venueId, coachId],
        );
        newSlotId = Number(gen.rows[0].id);
      } else {
        if (!slotRes.rows[0].is_available) {
          throw new ConflictError('That slot is not available');
        }
        newSlotId = Number(slotRes.rows[0].id);

        // Check for existing bookings on it
        const existing = await query(
          `SELECT 1 FROM bookings WHERE time_slot_id = $1 AND status IN ('pending','approved','confirmed')`,
          [newSlotId],
        );
        if (existing.rows.length > 0) {
          throw new ConflictError('That slot is already booked');
        }
      }
    }

    // Create the new booking — copy price, notes, venue/coach
    const newBooking = await query(
      `INSERT INTO bookings (player_id, time_slot_id, venue_id, coach_id, total_price, notes, status)
       VALUES ($1, $2, $3, $4, $5, $6, 'pending')
       RETURNING *`,
      [req.user!.id, newSlotId, venueId, coachId, src.total_price, src.notes],
    );

    created(res, {
      id: Number(newBooking.rows[0].id),
      playerId: Number(newBooking.rows[0].player_id),
      timeSlotId: Number(newBooking.rows[0].time_slot_id),
      venueId: newBooking.rows[0].venue_id ? Number(newBooking.rows[0].venue_id) : null,
      coachId: newBooking.rows[0].coach_id ? Number(newBooking.rows[0].coach_id) : null,
      status: newBooking.rows[0].status,
      totalPrice: Number(newBooking.rows[0].total_price),
      notes: newBooking.rows[0].notes,
      rebookedFrom: bookingId,
    }, 'Rebooked');
  } catch (e) {
    next(e);
  }
}
