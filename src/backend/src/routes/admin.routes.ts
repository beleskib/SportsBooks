// Admin-only routes — all gated by `requireAdmin`.
// Used by the owner dashboard at /admin (frontend).
import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { requireAdmin } from '../middleware/authorize';
import * as adminController from '../controllers/admin.controller';

const router = Router();

// Apply auth + admin gate to every route in this file.
router.use(authenticate, requireAdmin);

router.get('/overview', adminController.getOverview);

// Listing approval queue (venues + coaches) — see migration 0054.
router.get('/listings/pending', adminController.getPendingListings);
router.get('/listings/:type/:id', adminController.getListingDetail);
router.post('/listings/:type/:id/approve', adminController.approveListing);
router.post('/listings/:type/:id/reject', adminController.rejectListing);

export default router;
