import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as paymentController from '../controllers/payment.controller';

const router = Router();

router.post('/create-intent', authenticate, paymentController.createPaymentIntent);
router.post('/:id/confirm', authenticate, paymentController.confirmPayment);
router.post('/:id/fail', authenticate, paymentController.failPayment);
router.get('/mine', authenticate, paymentController.getMyPayments);
router.get('/booking/:bookingId', authenticate, paymentController.getByBookingId);
router.get('/:id', authenticate, paymentController.getById);

export default router;
