import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as discountRepo from '../repositories/discount.repository';

export async function getForVenue(req: Request, res: Response, next: NextFunction) {
  try {
    const discounts = await discountRepo.findByVenueId(Number(req.params.venueId));
    success(res, discounts);
  } catch (e) { next(e); }
}

export async function getForCoach(req: Request, res: Response, next: NextFunction) {
  try {
    const discounts = await discountRepo.findByCoachId(Number(req.params.coachId));
    success(res, discounts);
  } catch (e) { next(e); }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const discount = await discountRepo.create(req.body);
    created(res, discount, 'Discount created');
  } catch (e) { next(e); }
}

export async function update(req: Request, res: Response, next: NextFunction) {
  try {
    const discount = await discountRepo.update(Number(req.params.id), req.body);
    success(res, discount);
  } catch (e) { next(e); }
}
