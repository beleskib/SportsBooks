import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { createReviewSchema } from '../schemas/review.schema';
import * as reviewController from '../controllers/review.controller';

const router = Router();
router.post('/', authenticate, validate(createReviewSchema), reviewController.create);
router.get('/mine', authenticate, reviewController.getMine);
router.get('/venue/:venueId', authenticate, reviewController.getForVenue);
router.get('/coach/:coachId', authenticate, reviewController.getForCoach);
export default router;
