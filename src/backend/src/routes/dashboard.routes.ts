import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as dashboardController from '../controllers/dashboard.controller';

const router = Router();

router.get('/partner/stats', authenticate, dashboardController.getPartnerStats);

export default router;
