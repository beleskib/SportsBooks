import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requirePartnerOrAdmin } from '../middleware/authorize';
import * as coachController from '../controllers/coach.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, coachController.getAll);
router.post('/', authenticate, requirePartnerOrAdmin, coachController.create);
router.get('/top-deals', authenticate, coachController.getTopDeals);
router.get('/search', authenticate, coachController.search);
router.get('/mine', authenticate, coachController.getMine);
router.get('/by-sport/:sportType', authenticate, coachController.getBySport);
router.get('/:id', authenticate, coachController.getById);
router.put('/:id', authenticate, requirePartnerOrAdmin, coachController.update);
router.delete('/:id', authenticate, requirePartnerOrAdmin, coachController.remove);
// Image management
router.post('/:coachId/images', authenticate, requirePartnerOrAdmin, imageController.addCoachImage);
router.delete('/:coachId/images/:imageId', authenticate, requirePartnerOrAdmin, imageController.deleteCoachImage);
router.put('/:coachId/images/:imageId/primary', authenticate, requirePartnerOrAdmin, imageController.setCoachPrimaryImage);
export default router;
