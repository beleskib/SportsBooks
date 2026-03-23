import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requirePartnerOrAdmin } from '../middleware/authorize';
import * as venueController from '../controllers/venue.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, venueController.getAll);
router.post('/', authenticate, requirePartnerOrAdmin, venueController.create);
// Static routes BEFORE parameterized
router.get('/top-deals', authenticate, venueController.getTopDeals);
router.get('/search', authenticate, venueController.search);
router.get('/mine', authenticate, venueController.getMine);
router.get('/by-sport/:sportType', authenticate, venueController.getBySport);
router.get('/:id', authenticate, venueController.getById);
router.put('/:id', authenticate, requirePartnerOrAdmin, venueController.update);
router.delete('/:id', authenticate, requirePartnerOrAdmin, venueController.remove);
// Image management
router.post('/:venueId/images', authenticate, requirePartnerOrAdmin, imageController.addVenueImage);
router.delete('/:venueId/images/:imageId', authenticate, requirePartnerOrAdmin, imageController.deleteVenueImage);
router.put('/:venueId/images/:imageId/primary', authenticate, requirePartnerOrAdmin, imageController.setVenuePrimaryImage);
export default router;
