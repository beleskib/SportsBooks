import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as timeSlotRepo from '../repositories/timeSlot.repository';
import { NotFoundError, ValidationError } from '../utils/errors';

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
