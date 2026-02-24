import { Router, Request, Response, NextFunction } from 'express';
import * as venueRepo from '../repositories/venue.repository';
import { success } from '../utils/apiResponse';

const router = Router();

router.get('/top-deals', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const venues = await venueRepo.findTopDeals();
    success(res, venues);
  } catch (err) {
    next(err);
  }
});

router.get('/search', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const q = String(req.query.q || '');
    const venues = await venueRepo.search(q);
    success(res, venues);
  } catch (err) {
    next(err);
  }
});

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const sport = String(req.query.sport || '');
    if (!sport) return res.status(400).json({ success: false, error: { code: 'VALIDATION_ERROR', message: 'sport query param is required' } });
    const venues = await venueRepo.findBySport(sport);
    success(res, venues);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const id = parseInt(req.params.id, 10);
    const venue = await venueRepo.findById(id);
    if (!venue) return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'Venue not found' } });
    success(res, venue);
  } catch (err) {
    next(err);
  }
});

export default router;
