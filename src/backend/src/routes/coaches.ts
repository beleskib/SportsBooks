import { Router, Request, Response, NextFunction } from 'express';
import * as coachRepo from '../repositories/coach.repository';
import { success } from '../utils/apiResponse';

const router = Router();

router.get('/top-deals', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const coaches = await coachRepo.findTopDeals();
    success(res, coaches);
  } catch (err) {
    next(err);
  }
});

router.get('/search', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const q = String(req.query.q || '');
    const coaches = await coachRepo.search(q);
    success(res, coaches);
  } catch (err) {
    next(err);
  }
});

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const sport = String(req.query.sport || '');
    if (!sport) return res.status(400).json({ success: false, error: { code: 'VALIDATION_ERROR', message: 'sport query param is required' } });
    const coaches = await coachRepo.findBySport(sport);
    success(res, coaches);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const id = parseInt(req.params.id, 10);
    const coach = await coachRepo.findById(id);
    if (!coach) return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'Coach not found' } });
    success(res, coach);
  } catch (err) {
    next(err);
  }
});

export default router;
