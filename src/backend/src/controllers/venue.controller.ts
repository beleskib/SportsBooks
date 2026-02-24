import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as venueRepo from '../repositories/venue.repository';
import { NotFoundError } from '../utils/errors';

export async function getBySport(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findBySport(req.params.sportType);
    success(res, venues);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const venue = await venueRepo.findById(Number(req.params.id));
    if (!venue) throw new NotFoundError('Venue');
    success(res, venue);
  } catch (e) { next(e); }
}

export async function getTopDeals(_req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findTopDeals();
    success(res, venues);
  } catch (e) { next(e); }
}

export async function search(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.search(String(req.query.q || ''));
    success(res, venues);
  } catch (e) { next(e); }
}

export async function getMine(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findByOwnerId(req.user!.id);
    success(res, venues);
  } catch (e) { next(e); }
}
