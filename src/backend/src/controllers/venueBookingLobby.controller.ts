import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as venueBookingLobbyRepo from '../repositories/venueBookingLobby.repository';
import * as bookingRepo from '../repositories/booking.repository';
import * as paymentRepo from '../repositories/payment.repository';
import * as timeSlotRepo from '../repositories/timeSlot.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as notificationRepo from '../repositories/notification.repository';
import { NotFoundError, ForbiddenError, ValidationError, ConflictError } from '../utils/errors';
import { getStripe } from '../config/stripe';
import { resolvePartnerStripeAccountId } from '../services/stripeConnect.service';

const PLATFORM_FEE_PERCENT = 0.10; // 10% commission

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const { timeSlotId, title, paymentType, maxPlayers, description } = req.body;
    const userId = req.user!.id;

    if (!timeSlotId) throw new ValidationError('timeSlotId is required');
    if (!title) throw new ValidationError('title is required');
    if (!maxPlayers || maxPlayers < 2) throw new ValidationError('maxPlayers must be at least 2');
    if (!paymentType || !['split', 'creator_pays', 'split_to_teams'].includes(paymentType)) {
      throw new ValidationError('paymentType must be "split", "creator_pays", or "split_to_teams"');
    }

    // Look up the time slot to get venueId and total_price
    const timeSlot = await timeSlotRepo.findById(timeSlotId);
    if (!timeSlot) throw new NotFoundError('Time slot');
    if (!timeSlot.venueId) throw new ValidationError('Time slot must be linked to a venue');

    const venue = await venueRepo.findById(timeSlot.venueId);
    if (!venue) throw new NotFoundError('Venue');

    const totalPrice = timeSlot.priceOverride ?? venue.pricePerHour;

    const lobby = await venueBookingLobbyRepo.create(userId, {
      timeSlotId,
      venueId: timeSlot.venueId,
      title,
      paymentType,
      maxPlayers,
      totalPrice,
      description,
    });

    // Create default teams when split_to_teams payment type
    if (paymentType === 'split_to_teams') {
      await venueBookingLobbyRepo.createDefaultTeams(lobby.id, totalPrice, 2);
    }

    const finalLobby = paymentType === 'split_to_teams'
      ? await venueBookingLobbyRepo.findById(lobby.id)
      : lobby;

    created(res, finalLobby, 'Venue booking lobby created');
  } catch (e) { next(e); }
}

export async function updateLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;
    const { title, description, maxPlayers, paymentType } = req.body;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (Number(lobby.creatorId) !== userId) throw new ForbiddenError('Only the lobby creator can edit the lobby');
    if (lobby.status !== 'open' && lobby.status !== 'full') {
      throw new ConflictError('Lobby can only be edited when open or full');
    }

    if (title !== undefined && !title.trim()) {
      throw new ValidationError('Title cannot be empty');
    }
    if (maxPlayers !== undefined) {
      if (maxPlayers < 2) throw new ValidationError('maxPlayers must be at least 2');
      if (maxPlayers > 10) throw new ValidationError('maxPlayers must be at most 10');
      if (maxPlayers < lobby.currentPlayers) {
        throw new ValidationError(`Cannot set maxPlayers below current player count (${lobby.currentPlayers})`);
      }
    }
    if (paymentType !== undefined && !['split', 'creator_pays', 'split_to_teams'].includes(paymentType)) {
      throw new ValidationError('paymentType must be "split", "creator_pays", or "split_to_teams"');
    }

    // Handle payment type transitions
    const oldPaymentType = lobby.paymentType;
    const newPaymentType = paymentType ?? oldPaymentType;

    if (paymentType !== undefined && paymentType !== oldPaymentType) {
      // Switching AWAY from split_to_teams → remove all teams
      if (oldPaymentType === 'split_to_teams') {
        await venueBookingLobbyRepo.deleteAllTeams(lobbyId);
      }
      // Switching TO split_to_teams → create default teams
      if (newPaymentType === 'split_to_teams') {
        await venueBookingLobbyRepo.createDefaultTeams(lobbyId, lobby.totalPrice, 2);
      }
    }

    const updatedLobby = await venueBookingLobbyRepo.update(lobbyId, {
      title: title?.trim(),
      description: description !== undefined ? description : undefined,
      maxPlayers,
      paymentType,
    });

    success(res, updatedLobby, 'Lobby updated');
  } catch (e) { next(e); }
}

export async function listOpen(req: Request, res: Response, next: NextFunction) {
  try {
    const venueId = req.query.venueId ? Number(req.query.venueId) : undefined;
    const sportType = req.query.sportType ? String(req.query.sportType) : undefined;

    const lobbies = await venueBookingLobbyRepo.findOpen(venueId, sportType);
    success(res, lobbies);
  } catch (e) { next(e); }
}

export async function getMyLobbies(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbies = await venueBookingLobbyRepo.findByMember(req.user!.id);
    success(res, lobbies);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const lobby = await venueBookingLobbyRepo.findById(Number(req.params.id));
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    success(res, lobby);
  } catch (e) { next(e); }
}

export async function joinLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (lobby.status !== 'open') throw new ConflictError('Lobby is not open for joining');

    // Check if user is already a joined member
    const existingMember = lobby.members.find(m => Number(m.userId) === userId && m.status === 'joined');
    if (existingMember) throw new ConflictError('You are already a member of this lobby');

    await venueBookingLobbyRepo.join(lobbyId, userId);
    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);

    // Notify all members when lobby is full
    if (updatedLobby && updatedLobby.status === 'full') {
      try {
        for (const member of updatedLobby.members) {
          if (member.status === 'joined') {
            await notificationRepo.createNotification(
              member.userId,
              'lobby_full',
              'Lobby is Full!',
              `"${updatedLobby.title}" is now full with ${updatedLobby.maxPlayers} players. The creator can now submit the booking.`,
              { lobbyId: String(lobbyId) }
            );
          }
        }
      } catch (e) {
        console.error('Failed to send lobby full notifications:', e);
      }
    }

    success(res, updatedLobby, 'Joined lobby successfully');
  } catch (e) { next(e); }
}

export async function leaveLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (Number(lobby.creatorId) === userId) throw new ForbiddenError('Lobby creator cannot leave. Cancel the lobby instead.');
    if (lobby.status !== 'open' && lobby.status !== 'full') {
      throw new ConflictError('Cannot leave lobby in current status');
    }

    const left = await venueBookingLobbyRepo.leave(lobbyId, userId);
    if (!left) throw new NotFoundError('Lobby membership');

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Left lobby successfully');
  } catch (e) { next(e); }
}

export async function createBookingFromLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (Number(lobby.creatorId) !== userId) throw new ForbiddenError('Only the lobby creator can create a booking');
    if (lobby.currentPlayers < 2) throw new ValidationError('Lobby must have at least 2 players');
    if (lobby.status !== 'open' && lobby.status !== 'full') {
      throw new ConflictError('Lobby must be open or full to create a booking');
    }

    // Create booking via the booking repository
    const booking = await bookingRepo.create(userId, lobby.timeSlotId);

    // Link booking to lobby
    await venueBookingLobbyRepo.setBookingId(lobbyId, booking.id);

    // Set lobby status to booking_pending
    await venueBookingLobbyRepo.updateStatus(lobbyId, 'booking_pending');

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Booking created from lobby');
  } catch (e) { next(e); }
}

export async function cancelLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (Number(lobby.creatorId) !== userId) throw new ForbiddenError('Only the lobby creator can cancel the lobby');

    await venueBookingLobbyRepo.updateStatus(lobbyId, 'cancelled');

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Lobby cancelled');
  } catch (e) { next(e); }
}

export async function createSplitPaymentIntent(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (lobby.status !== 'booking_approved' && lobby.status !== 'payment_in_progress') {
      throw new ValidationError('Lobby must be in booking_approved or payment_in_progress status');
    }
    if (!lobby.bookingId) throw new ValidationError('Lobby has no linked booking');

    // Find the member record for this user
    const member = lobby.members.find(m => Number(m.userId) === userId && m.status !== 'left');
    if (!member) throw new ForbiddenError('You are not a member of this lobby');
    if (member.status === 'paid') throw new ConflictError('You have already paid');

    // Determine the amount
    let amount: number;
    if (lobby.paymentType === 'creator_pays') {
      if (Number(lobby.creatorId) !== userId) {
        throw new ForbiddenError('Only the creator pays in creator_pays mode');
      }
      amount = lobby.totalPrice;
    } else if (lobby.paymentType === 'split_to_teams') {
      // Only team leaders pay in split_to_teams mode
      const team = lobby.teams.find(t => Number(t.leaderId) === userId);
      if (!team) {
        throw new ForbiddenError('Only team leaders can pay in split_to_teams mode');
      }
      amount = team.shareAmount ?? (lobby.totalPrice / lobby.teams.length);
    } else {
      // split payment
      amount = member.shareAmount ?? lobby.pricePerPlayer;
    }

    // Dev mode bypass: skip Stripe when DEV_AUTH_BYPASS is true
    if (process.env.DEV_AUTH_BYPASS === 'true') {
      const devPaymentId = `dev_pi_${Date.now()}_lobby_${lobbyId}_${userId}`;
      const platformFee = Math.round(amount * PLATFORM_FEE_PERCENT);
      const payment = await paymentRepo.create(
        lobby.bookingId,
        userId,
        amount,
        'MKD',
        devPaymentId,
        platformFee
      );

      // Update member's payment_id and set lobby status to payment_in_progress
      await venueBookingLobbyRepo.updateMemberPaymentStatus(
        lobbyId, userId, payment.id, 'payment_pending'
      );
      await venueBookingLobbyRepo.updateStatus(lobbyId, 'payment_in_progress');

      created(res, {
        clientSecret: `dev_secret_${devPaymentId}`,
        lobbyId,
        bookingId: lobby.bookingId,
        paymentId: payment.id,
        amount,
        currency: 'MKD',
      }, 'Payment intent created');
      return;
    }

    // Production: Create Stripe PaymentIntent
    const stripe = getStripe();
    const amountInCents = Math.round(amount * 100);
    const partnerStripeAccountId = await resolvePartnerStripeAccountId(
      lobby.venueId ?? null,
      null
    );

    const paymentIntentParams: Record<string, any> = {
      amount: amountInCents,
      currency: 'mkd',
      metadata: {
        lobbyId: String(lobbyId),
        bookingId: String(lobby.bookingId),
        playerId: String(userId),
        paymentType: lobby.paymentType,
      },
      automatic_payment_methods: { enabled: true },
    };

    let platformFeeAmount: number | undefined;
    if (partnerStripeAccountId) {
      const feeInCents = Math.round(amountInCents * PLATFORM_FEE_PERCENT);
      paymentIntentParams.application_fee_amount = feeInCents;
      paymentIntentParams.transfer_data = { destination: partnerStripeAccountId };
      platformFeeAmount = feeInCents / 100;
    }

    const paymentIntent = await stripe.paymentIntents.create(paymentIntentParams as any);

    // Create payment record
    const payment = await paymentRepo.create(
      lobby.bookingId,
      userId,
      amount,
      'MKD',
      paymentIntent.id,
      platformFeeAmount
    );

    // Update member's payment_id and set lobby status to payment_in_progress
    await venueBookingLobbyRepo.updateMemberPaymentStatus(
      lobbyId, userId, payment.id, 'payment_pending'
    );
    await venueBookingLobbyRepo.updateStatus(lobbyId, 'payment_in_progress');

    created(res, {
      clientSecret: paymentIntent.client_secret!,
      lobbyId,
      bookingId: lobby.bookingId,
      paymentId: payment.id,
      amount,
      currency: 'MKD',
    }, 'Payment intent created');
  } catch (e) { next(e); }
}

export async function confirmMemberPayment(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');

    // Find the member
    const member = lobby.members.find(m => Number(m.userId) === userId && m.status !== 'left');
    if (!member) throw new ForbiddenError('You are not a member of this lobby');
    if (member.status === 'paid') throw new ConflictError('Payment already confirmed');
    if (!member.paymentId) throw new ValidationError('No payment found for this member. Create a payment intent first.');

    // Verify with Stripe that payment succeeded (skip in dev mode)
    const payment = await paymentRepo.findById(member.paymentId);
    if (!payment) throw new NotFoundError('Payment');

    if (payment.externalPaymentId && !payment.externalPaymentId.startsWith('dev_')) {
      const stripe = getStripe();
      const pi = await stripe.paymentIntents.retrieve(payment.externalPaymentId);
      if (pi.status !== 'succeeded') {
        throw new ValidationError('Payment has not succeeded on Stripe');
      }
    }

    // Update payment status to completed
    await paymentRepo.updateStatus(member.paymentId, 'completed', new Date().toISOString());

    // Update member status to paid
    await venueBookingLobbyRepo.updateMemberPaymentStatus(
      lobbyId, userId, member.paymentId, 'paid', new Date().toISOString()
    );

    // Check if all payments are complete
    const allPaid = lobby.paymentType === 'split_to_teams'
      ? await venueBookingLobbyRepo.areAllTeamLeadersPaid(lobbyId)
      : await venueBookingLobbyRepo.areAllMembersPaid(lobbyId);
    if (allPaid) {
      // Set lobby to confirmed and booking to confirmed
      await venueBookingLobbyRepo.updateStatus(lobbyId, 'confirmed');
      if (lobby.bookingId) {
        await bookingRepo.updateStatus(lobby.bookingId, 'confirmed');
      }
    }

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, allPaid ? 'All payments confirmed. Booking confirmed!' : 'Payment confirmed');
  } catch (e) { next(e); }
}

// ── Team endpoints ──────────────────────────────────────────────────────────

export async function getTeams(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    const teams = await venueBookingLobbyRepo.getTeams(lobbyId);
    success(res, teams);
  } catch (e) { next(e); }
}

export async function addTeam(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;
    const { teamName } = req.body;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');
    if (Number(lobby.creatorId) !== userId) throw new ForbiddenError('Only the lobby creator can add teams');
    if (lobby.status !== 'open' && lobby.status !== 'full') {
      throw new ConflictError('Cannot add teams in current lobby status');
    }
    if (!teamName) throw new ValidationError('teamName is required');

    const team = await venueBookingLobbyRepo.addTeam(lobbyId, teamName);

    // Refresh to get updated share amounts
    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Team added');
  } catch (e) { next(e); }
}

export async function joinTeam(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const teamId = Number(req.params.teamId);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');

    // Verify user is a member of the lobby
    const member = lobby.members.find(m => Number(m.userId) === userId && m.status !== 'left');
    if (!member) throw new ForbiddenError('You must be a lobby member to join a team');

    // Verify team belongs to this lobby
    const team = lobby.teams.find(t => Number(t.id) === teamId);
    if (!team) throw new NotFoundError('Team');

    await venueBookingLobbyRepo.joinTeam(lobbyId, userId, teamId);

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Joined team');
  } catch (e) { next(e); }
}

export async function leaveTeam(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const userId = req.user!.id;

    const lobby = await venueBookingLobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Venue booking lobby');

    const member = lobby.members.find(m => Number(m.userId) === userId && m.status !== 'left');
    if (!member) throw new ForbiddenError('You are not a member of this lobby');
    if (!member.teamId) throw new ConflictError('You are not in a team');

    await venueBookingLobbyRepo.leaveTeam(lobbyId, userId);

    const updatedLobby = await venueBookingLobbyRepo.findById(lobbyId);
    success(res, updatedLobby, 'Left team');
  } catch (e) { next(e); }
}
