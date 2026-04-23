import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as homeController from '../controllers/v2.home.controller';
import * as playController from '../controllers/v2.play.controller';
import * as rebookController from '../controllers/v2.rebook.controller';
import * as splitPaymentController from '../controllers/v2.splitPayment.controller';
import * as participantController from '../controllers/v2.bookingParticipant.controller';

// ============================================================
// v2-practical-ux routes
// Mounted at /api/*; keeps Option 1 routes untouched.
// ============================================================

const router = Router();

// Unified home feed
router.get('/home/feed', authenticate, homeController.getHomeFeed);

// Unified Play search
router.get('/play/search', authenticate, playController.searchPlay);

// One-tap rebook
router.post('/bookings/:id/rebook', authenticate, rebookController.rebookFromBooking);

// Split payments
router.post('/bookings/:id/split', authenticate, splitPaymentController.createSplit);
router.get('/bookings/:id/split', authenticate, splitPaymentController.getSplitSummary);
router.post('/split-payments/:shareId/pay', authenticate, splitPaymentController.payShare);

// Booking participants
router.post('/bookings/:id/invite', authenticate, participantController.inviteParticipants);
router.post('/bookings/:id/respond', authenticate, participantController.respondToInvite);
router.post('/bookings/:id/attendance', authenticate, participantController.markAttendance);
router.get('/bookings/:id/participants', authenticate, participantController.listParticipants);

export default router;
