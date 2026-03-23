import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as bookingController from '../controllers/booking.controller';

const router = Router();
router.post('/', authenticate, bookingController.create);
router.get('/counts', authenticate, bookingController.getBookingCounts);
router.get('/mine', authenticate, bookingController.getMyBookings);
router.get('/partner', authenticate, bookingController.getPartnerBookings);
router.get('/:id', authenticate, bookingController.getById);
router.put('/:id/status', authenticate, bookingController.updateStatus);
export default router;
