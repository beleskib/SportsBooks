import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as cardController from '../controllers/card.controller';

const router = Router();

// POST /api/cards/setup-intent — Create SetupIntent to save a new card
router.post('/setup-intent', authenticate, cardController.createSetupIntent);

// GET /api/cards — List saved cards
router.get('/', authenticate, cardController.listCards);

// DELETE /api/cards/:paymentMethodId — Remove a saved card
router.delete('/:paymentMethodId', authenticate, cardController.deleteCard);

// PUT /api/cards/:paymentMethodId/default — Set default card
router.put('/:paymentMethodId/default', authenticate, cardController.setDefault);

export default router;
