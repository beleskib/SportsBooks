import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as bookingRepo from '../repositories/booking.repository';
import { NotFoundError } from '../utils/errors';

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const { timeSlotId, notes } = req.body;
    const booking = await bookingRepo.create(req.user!.id, timeSlotId, notes);
    created(res, booking, 'Booking created');
  } catch (e) { next(e); }
}

export async function getMyBookings(req: Request, res: Response, next: NextFunction) {
  try {
    const status = req.query.status ? String(req.query.status) : undefined;
    const bookings = await bookingRepo.findByPlayerId(req.user!.id, status);
    success(res, bookings);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const booking = await bookingRepo.findById(Number(req.params.id));
    if (!booking) throw new NotFoundError('Booking');
    success(res, booking);
  } catch (e) { next(e); }
}

export async function updateStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const { status } = req.body;
    const booking = await bookingRepo.updateStatus(Number(req.params.id), status);
    success(res, booking);
  } catch (e) { next(e); }
}

export async function getPartnerBookings(req: Request, res: Response, next: NextFunction) {
  try {
    const status = req.query.status ? String(req.query.status) : undefined;
    const bookings = await bookingRepo.findByPartner(req.user!.id, status);
    success(res, bookings);
  } catch (e) { next(e); }
}
