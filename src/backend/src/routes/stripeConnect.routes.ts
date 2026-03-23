import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as stripeConnectController from '../controllers/stripeConnect.controller';

const router = Router();

router.post('/onboard', authenticate, stripeConnectController.onboard);
router.get('/status', authenticate, stripeConnectController.getStatus);
router.post('/dashboard-link', authenticate, stripeConnectController.getDashboardLink);

export default router;
