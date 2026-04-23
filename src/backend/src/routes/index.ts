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
import paymentRoutes from './payment.routes';
import matchRoutes from './match.routes';
import notificationRoutes from './notification.routes';
import searchRoutes from './search.routes';
import favoriteRoutes from './favorite.routes';
import friendshipRoutes from './friendship.routes';
import partyRoutes from './party.routes';
import stripeConnectRoutes from './stripeConnect.routes';
import availablePlayerRoutes from './availablePlayer.routes';
import gamificationRoutes from './gamification.routes';
import dashboardRoutes from './dashboard.routes';
import bookingChatRoutes from './bookingChat.routes';
import feedRoutes from './feed.routes';
import communityRoutes from './community.routes';
import lobbyRoutes from './lobby.routes';
import venueBookingLobbyRoutes from './venueBookingLobby.routes';
import v2Routes from './v2.routes';

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
router.use('/payments', paymentRoutes);
router.use('/matches', matchRoutes);
router.use('/notifications', notificationRoutes);
router.use('/search', searchRoutes);
router.use('/favorites', favoriteRoutes);
router.use('/friends', friendshipRoutes);
router.use('/parties', partyRoutes);
router.use('/stripe-connect', stripeConnectRoutes);
router.use('/available-players', availablePlayerRoutes);
router.use('/gamification', gamificationRoutes);
router.use('/dashboard', dashboardRoutes);
router.use('/bookings', bookingChatRoutes);
router.use('/feed', feedRoutes);
router.use('/communities', communityRoutes);
router.use('/', lobbyRoutes);  // handles both /communities/:id/lobbies and /lobbies/*
router.use('/venue-booking-lobbies', venueBookingLobbyRoutes);
router.use('/', v2Routes);  // v2-practical-ux: /home/feed, /play/search, /bookings/:id/rebook, /split-payments/*, etc.

export default router;
