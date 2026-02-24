import { Router, Request, Response, NextFunction } from 'express';
import * as sportRepo from '../repositories/sport.repository';
import { success } from '../utils/apiResponse';

const router = Router();

router.get('/', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const categories = await sportRepo.findAll();
    success(res, categories);
  } catch (err) {
    next(err);
  }
});

router.get('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const id = parseInt(req.params.id, 10);
    const category = await sportRepo.findById(id);
    if (!category) return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'Sport category not found' } });
    success(res, category);
  } catch (err) {
    next(err);
  }
});

export default router;
