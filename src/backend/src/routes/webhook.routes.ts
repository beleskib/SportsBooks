import { Router } from 'express';
import express from 'express';
import * as webhookController from '../controllers/webhook.controller';

const router = Router();

// Stripe webhooks need raw body for signature verification
router.post('/stripe', express.raw({ type: 'application/json' }), webhookController.handleStripeWebhook);

export default router;
