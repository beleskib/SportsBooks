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

router.get('/partners', adminController.getAllPartners);
router.get('/partners/pending', adminController.getPendingPartners);
router.get('/partners/:id', adminController.getPartner);
router.post('/partners/:id/approve', adminController.approvePartner);
router.post('/partners/:id/reject', adminController.rejectPartner);

export default router;
