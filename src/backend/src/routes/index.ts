import { Router } from 'express';
import authRoutes from './auth.routes';
import userRoutes from './user.routes';
import sportRoutes from './sport.routes';
import venueRoutes from './venue.routes';
import coachRoutes from './coach.routes';
import timeSlotRoutes from './timeSlot.routes';
import bookingRoutes from './booking.routes';
import reviewRoutes from './review.routes';
import discountRoutes from './discount.routes';

const router = Router();

router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/sports', sportRoutes);
router.use('/venues', venueRoutes);
router.use('/coaches', coachRoutes);
router.use('/', timeSlotRoutes);
router.use('/bookings', bookingRoutes);
router.use('/reviews', reviewRoutes);
router.use('/discounts', discountRoutes);

export default router;
