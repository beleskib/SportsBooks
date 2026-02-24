import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as timeSlotController from '../controllers/timeSlot.controller';

const router = Router();
router.get('/venues/:venueId/time-slots', authenticate, timeSlotController.getVenueSlots);
router.get('/coaches/:coachId/time-slots', authenticate, timeSlotController.getCoachSlots);
router.get('/time-slots/:id', authenticate, timeSlotController.getById);
export default router;
