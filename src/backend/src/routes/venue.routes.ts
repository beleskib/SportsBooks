import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requireApprovedPartner } from '../middleware/authorize';
import * as venueController from '../controllers/venue.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, venueController.getAll);
// Write operations require an approved partner (or admin).
router.post('/', authenticate, requireApprovedPartner, venueController.create);
// Static routes BEFORE parameterized
router.get('/top-deals', authenticate, venueController.getTopDeals);
router.get('/search', authenticate, venueController.search);
// /mine intentionally allowed for unapproved partners — they need to see their drafts.
router.get('/mine', authenticate, venueController.getMine);
router.get('/by-sport/:sportType', authenticate, venueController.getBySport);
router.get('/:id', authenticate, venueController.getById);
router.put('/:id', authenticate, requireApprovedPartner, venueController.update);
router.delete('/:id', authenticate, requireApprovedPartner, venueController.remove);
// Image management — same gate as the venue write ops it serves.
router.post('/:venueId/images', authenticate, requireApprovedPartner, imageController.addVenueImage);
router.delete('/:venueId/images/:imageId', authenticate, requireApprovedPartner, imageController.deleteVenueImage);
router.put('/:venueId/images/:imageId/primary', authenticate, requireApprovedPartner, imageController.setVenuePrimaryImage);
export default router;
