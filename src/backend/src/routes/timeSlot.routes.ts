import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requirePartnerOrAdmin } from '../middleware/authorize';
import { validate } from '../middleware/validate';
import { generateSlotsSchema } from '../schemas/timeSlot.schema';
import * as timeSlotController from '../controllers/timeSlot.controller';

const router = Router();
router.post('/time-slots/generate', authenticate, requirePartnerOrAdmin, validate(generateSlotsSchema), timeSlotController.generateSlots);
router.get('/venues/:venueId/time-slots', authenticate, timeSlotController.getVenueSlots);
router.get('/coaches/:coachId/time-slots', authenticate, timeSlotController.getCoachSlots);
router.get('/time-slots/:id', authenticate, timeSlotController.getById);
router.delete('/time-slots/:id', authenticate, requirePartnerOrAdmin, timeSlotController.deleteSlot);
export default router;
