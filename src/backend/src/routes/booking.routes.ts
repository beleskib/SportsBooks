import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as bookingController from '../controllers/booking.controller';
import * as receiptController from '../controllers/receipt.controller';

const router = Router();
router.post('/', authenticate, bookingController.create);
router.get('/counts', authenticate, bookingController.getBookingCounts);
router.get('/mine', authenticate, bookingController.getMyBookings);
router.get('/partner', authenticate, bookingController.getPartnerBookings);
router.put('/:id/approve', authenticate, bookingController.approveBooking);
router.put('/:id/decline', authenticate, bookingController.declineBooking);
router.get('/:id/receipt', authenticate, receiptController.getBookingReceipt);
router.get('/:id', authenticate, bookingController.getById);
router.put('/:id/status', authenticate, bookingController.updateStatus);
export default router;
