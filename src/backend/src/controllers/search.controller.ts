import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import * as matchRepo from '../repositories/match.repository';

export async function globalSearch(req: Request, res: Response, next: NextFunction) {
  try {
    const q = String(req.query.q || '');
    const limit = req.query.limit ? Number(req.query.limit) : 5;

    if (!q.trim()) {
      success(res, { venues: [], coaches: [], matches: [] });
      return;
    }

    const [venues, coaches, matches] = await Promise.all([
      venueRepo.search(q),
      coachRepo.search(q),
      matchRepo.search(q),
    ]);

    success(res, {
      venues: venues.slice(0, limit),
      coaches: coaches.slice(0, limit),
      matches: matches.slice(0, limit),
    });
  } catch (e) { next(e); }
}
