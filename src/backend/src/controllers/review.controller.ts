import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as reviewRepo from '../repositories/review.repository';

export async function getForVenue(req: Request, res: Response, next: NextFunction) {
  try {
    const reviews = await reviewRepo.findByVenueId(Number(req.params.venueId));
    success(res, reviews);
  } catch (e) { next(e); }
}

export async function getForCoach(req: Request, res: Response, next: NextFunction) {
  try {
    const reviews = await reviewRepo.findByCoachId(Number(req.params.coachId));
    success(res, reviews);
  } catch (e) { next(e); }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const review = await reviewRepo.create({ playerId: req.user!.id, ...req.body });
    created(res, review, 'Review created');
  } catch (e) { next(e); }
}

export async function getMine(req: Request, res: Response, next: NextFunction) {
  try {
    const reviews = await reviewRepo.findByPlayerId(req.user!.id);
    success(res, reviews);
  } catch (e) { next(e); }
}
