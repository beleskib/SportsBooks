import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requirePartnerOrAdmin } from '../middleware/authorize';
import { validate } from '../middleware/validate';
import { createCoachSchema, updateCoachSchema } from '../schemas/coach.schema';
import * as coachController from '../controllers/coach.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, coachController.getAll);
// Write operations require a partner or admin. The listing-level approval
// gate (see migration 0054) hides pending coaches from the public; the
// partner can still create + edit them.
router.post('/', authenticate, requirePartnerOrAdmin, validate(createCoachSchema), coachController.create);
router.get('/top-deals', authenticate, coachController.getTopDeals);
router.get('/search', authenticate, coachController.search);
router.get('/mine', authenticate, coachController.getMine);
router.get('/by-sport/:sportType', authenticate, coachController.getBySport);
router.get('/:id', authenticate, coachController.getById);
router.put('/:id', authenticate, requirePartnerOrAdmin, validate(updateCoachSchema), coachController.update);
router.delete('/:id', authenticate, requirePartnerOrAdmin, coachController.remove);
// Image management — same gate as the coach write ops it serves.
router.post('/:coachId/images', authenticate, requirePartnerOrAdmin, imageController.addCoachImage);
router.delete('/:coachId/images/:imageId', authenticate, requirePartnerOrAdmin, imageController.deleteCoachImage);
router.put('/:coachId/images/:imageId/primary', authenticate, requirePartnerOrAdmin, imageController.setCoachPrimaryImage);
export default router;
