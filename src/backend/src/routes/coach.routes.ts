import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as coachController from '../controllers/coach.controller';

const router = Router();
router.get('/top-deals', authenticate, coachController.getTopDeals);
router.get('/search', authenticate, coachController.search);
router.get('/mine', authenticate, coachController.getMine);
router.get('/by-sport/:sportType', authenticate, coachController.getBySport);
router.get('/:id', authenticate, coachController.getById);
export default router;
