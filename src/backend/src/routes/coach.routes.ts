import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requireApprovedPartner } from '../middleware/authorize';
import * as coachController from '../controllers/coach.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, coachController.getAll);
// Write operations require an approved partner (or admin).
router.post('/', authenticate, requireApprovedPartner, coachController.create);
router.get('/top-deals', authenticate, coachController.getTopDeals);
router.get('/search', authenticate, coachController.search);
// /mine intentionally allowed for unapproved partners — they need to see their drafts.
router.get('/mine', authenticate, coachController.getMine);
router.get('/by-sport/:sportType', authenticate, coachController.getBySport);
router.get('/:id', authenticate, coachController.getById);
router.put('/:id', authenticate, requireApprovedPartner, coachController.update);
router.delete('/:id', authenticate, requireApprovedPartner, coachController.remove);
// Image management — same gate as the coach write ops it serves.
router.post('/:coachId/images', authenticate, requireApprovedPartner, imageController.addCoachImage);
router.delete('/:coachId/images/:imageId', authenticate, requireApprovedPartner, imageController.deleteCoachImage);
router.put('/:coachId/images/:imageId/primary', authenticate, requireApprovedPartner, imageController.setCoachPrimaryImage);
export default router;
