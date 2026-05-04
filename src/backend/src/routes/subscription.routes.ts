import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as subController from '../controllers/subscription.controller';

const router = Router();

// GET  /api/subscription        — current subscription status
// POST /api/subscription/checkout — create Stripe Checkout session
// POST /api/subscription/cancel   — cancel at end of period
// POST /api/subscription/reactivate — un-cancel
// PUT  /api/subscription/visibility — set profile visibility (Plus)

router.get('/', authenticate, subController.getSubscription);
router.post('/checkout', authenticate, subController.createCheckout);
router.post('/cancel', authenticate, subController.cancelSubscription);
router.post('/reactivate', authenticate, subController.reactivateSubscription);
router.put('/visibility', authenticate, subController.setVisibility);

export default router;
