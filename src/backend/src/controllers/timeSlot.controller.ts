import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as timeSlotRepo from '../repositories/timeSlot.repository';
import { NotFoundError, ValidationError, ForbiddenError } from '../utils/errors';
import { query } from '../config/database';

export async function getVenueSlots(req: Request, res: Response, next: NextFunction) {
  try {
    const { dateFrom, dateTo } = req.query;
    if (!dateFrom || !dateTo) throw new ValidationError('dateFrom and dateTo are required');
    const slots = await timeSlotRepo.findByVenue(Number(req.params.venueId), String(dateFrom), String(dateTo));
    success(res, slots);
  } catch (e) { next(e); }
}

export async function getCoachSlots(req: Request, res: Response, next: NextFunction) {
  try {
    const { dateFrom, dateTo } = req.query;
    if (!dateFrom || !dateTo) throw new ValidationError('dateFrom and dateTo are required');
    const slots = await timeSlotRepo.findByCoach(Number(req.params.coachId), String(dateFrom), String(dateTo));
    success(res, slots);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const slot = await timeSlotRepo.findById(Number(req.params.id));
    if (!slot) throw new NotFoundError('Time slot');
    success(res, slot);
  } catch (e) { next(e); }
}

export async function generateSlots(req: Request, res: Response, next: NextFunction) {
  try {
    const { venueId, coachId, dateFrom, dateTo, startHour, endHour, daysOfWeek } = req.body;
    if (!dateFrom || !dateTo) throw new ValidationError('dateFrom and dateTo are required');
    if (!venueId && !coachId) throw new ValidationError('venueId or coachId is required');

    // Configurable operating hours (default 09:00–22:00)
    const opStart = typeof startHour === 'number' ? Math.max(0, Math.min(23, startHour)) : 9;
    const opEnd = typeof endHour === 'number' ? Math.max(1, Math.min(24, endHour)) : 22;
    if (opStart >= opEnd) throw new ValidationError('startHour must be less than endHour');

    // Optional day-of-week filter: 0=Sun, 1=Mon, ..., 6=Sat (default all days)
    const allowedDays: number[] | null = Array.isArray(daysOfWeek) && daysOfWeek.length > 0
      ? daysOfWeek.map(Number)
      : null;

    const slots: { venueId?: number; coachId?: number; slotDate: string; startTime: string; endTime: string }[] = [];
    const start = new Date(dateFrom);
    const end = new Date(dateTo);

    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      // Skip days not in the allowed list
      if (allowedDays && !allowedDays.includes(d.getDay())) continue;

      const dateStr = d.toISOString().split('T')[0];
      for (let hour = opStart; hour < opEnd; hour++) {
        const startTime = `${hour.toString().padStart(2, '0')}:00`;
        const endTime = `${(hour + 1).toString().padStart(2, '0')}:00`;
        slots.push({
          venueId: venueId ? Number(venueId) : undefined,
          coachId: coachId ? Number(coachId) : undefined,
          slotDate: dateStr,
          startTime,
          endTime,
        });
      }
    }

    const createdSlots = await timeSlotRepo.createBatch(slots);
    created(res, createdSlots);
  } catch (e) { next(e); }
}

export async function deleteSlot(req: Request, res: Response, next: NextFunction) {
  try {
    const id = Number(req.params.id);
    const slot = await timeSlotRepo.findById(id);
    if (!slot) throw new NotFoundError('Time slot');

    // Verify ownership: check that the slot's venue/coach belongs to the requesting user
    if (req.user!.role !== 'admin') {
      let isOwner = false;
      if (slot.venueId) {
        const result = await query('SELECT owner_id FROM venues WHERE id = $1', [slot.venueId]);
        isOwner = result.rows.length > 0 && result.rows[0].owner_id === req.user!.id;
      } else if (slot.coachId) {
        const result = await query('SELECT user_id FROM coaches WHERE id = $1', [slot.coachId]);
        isOwner = result.rows.length > 0 && result.rows[0].user_id === req.user!.id;
      }
      if (!isOwner) {
        throw new ForbiddenError('You can only delete time slots for your own venues or coach profile');
      }
    }

    await timeSlotRepo.deleteById(id);
    success(res, { id, message: 'Time slot deleted' });
  } catch (e) { next(e); }
}
