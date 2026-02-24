import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as discountController from '../controllers/discount.controller';

const router = Router();
router.post('/', authenticate, discountController.create);
router.put('/:id', authenticate, discountController.update);
router.get('/venue/:venueId', authenticate, discountController.getForVenue);
router.get('/coach/:coachId', authenticate, discountController.getForCoach);
export default router;
