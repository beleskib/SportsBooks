import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as venueController from '../controllers/venue.controller';

const router = Router();
// Static routes BEFORE parameterized
router.get('/top-deals', authenticate, venueController.getTopDeals);
router.get('/search', authenticate, venueController.search);
router.get('/mine', authenticate, venueController.getMine);
router.get('/by-sport/:sportType', authenticate, venueController.getBySport);
router.get('/:id', authenticate, venueController.getById);
export default router;
