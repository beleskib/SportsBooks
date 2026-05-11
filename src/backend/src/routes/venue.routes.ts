import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requirePartnerOrAdmin } from '../middleware/authorize';
import { validate } from '../middleware/validate';
import { createVenueSchema, updateVenueSchema } from '../schemas/venue.schema';
import * as venueController from '../controllers/venue.controller';
import * as imageController from '../controllers/image.controller';

const router = Router();
router.get('/', authenticate, venueController.getAll);
// Write operations require a partner or admin. The listing-level approval
// gate (see migration 0054) hides pending venues from the public; the
// partner can still create + edit them.
router.post('/', authenticate, requirePartnerOrAdmin, validate(createVenueSchema), venueController.create);
// Static routes BEFORE parameterized
router.get('/top-deals', authenticate, venueController.getTopDeals);
router.get('/search', authenticate, venueController.search);
router.get('/mine', authenticate, venueController.getMine);
router.get('/by-sport/:sportType', authenticate, venueController.getBySport);
router.get('/:id', authenticate, venueController.getById);
router.put('/:id', authenticate, requirePartnerOrAdmin, validate(updateVenueSchema), venueController.update);
router.delete('/:id', authenticate, requirePartnerOrAdmin, venueController.remove);
// Image management — same gate as the venue write ops it serves.
router.post('/:venueId/images', authenticate, requirePartnerOrAdmin, imageController.addVenueImage);
router.delete('/:venueId/images/:imageId', authenticate, requirePartnerOrAdmin, imageController.deleteVenueImage);
router.put('/:venueId/images/:imageId/primary', authenticate, requirePartnerOrAdmin, imageController.setVenuePrimaryImage);
export default router;
