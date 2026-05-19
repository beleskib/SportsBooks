import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as matchRepo from '../repositories/match.repository';
import * as userRepo from '../repositories/user.repository';
import * as timeSlotRepo from '../repositories/timeSlot.repository';
import * as bookingRepo from '../repositories/booking.repository';
import * as splitRepo from '../repositories/splitPayment.repository';
import * as participantRepo from '../repositories/bookingParticipant.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as partyRepo from '../repositories/party.repository';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import * as notificationService from '../services/notification.service';
import { getFirestoreDb } from '../config/firebase';
import { query } from '../config/database';
import { sendNotification } from '../services/notification.service';

// ============================================================
// Firestore sync helper
// ============================================================

async function syncMatchToFirestore(matchId: number): Promise<void> {
  try {
    const match = await matchRepo.findById(matchId);
    if (!match) return;
    const db = getFirestoreDb();
    if (!db) return;
    await db.collection('matches').doc(String(matchId)).set({
      id: match.id,
      title: match.title,
      status: match.status,
      currentPlayers: match.currentPlayers,
      maxPlayers: match.maxPlayers,
      paymentType: match.paymentType,
      totalPrice: match.totalPrice,
      pricePerPlayer: match.pricePerPlayer,
      currency: match.currency,
      participants: match.participants.map((p) => ({
        userId: p.userId,
        userName: p.userName,
        userPhotoUrl: p.userPhotoUrl,
        role: p.role,
        status: p.status,
      })),
      updatedAt: Date.now(),
    }, { merge: true });
  } catch (e) {
    console.error('Firestore match sync failed:', e);
  }
}

// ============================================================
// Auto-complete past matches
// ============================================================

/**
 * Auto-complete past matches whose end time has passed.
 * When a match's match_date + end_time is in the past and the match
 * is still open, full, or in_progress, we mark it as 'completed'.
 * Called lazily when matches are fetched — no cron job required.
 */
async function autoCompletePastMatches(): Promise<void> {
  try {
    const result = await query(
      `UPDATE matches m
       SET status = 'completed', updated_at = NOW()
       WHERE m.status IN ('open', 'full', 'in_progress')
         AND (m.match_date + m.end_time::time) < NOW()
       RETURNING m.id, m.host_id,
         (SELECT ARRAY_AGG(mp.user_id)
          FROM match_participants mp
          WHERE mp.match_id = m.id AND mp.status = 'approved'
         ) AS participant_user_ids`
    );

    for (const row of result.rows) {
      const matchId = Number(row.id);

      // Sync each auto-completed match to Firestore
      syncMatchToFirestore(matchId).catch(() => {});

      // Send rating reminder notification to all participants (including host)
      const participantIds: number[] = row.participant_user_ids ?? [];
      const allUserIds = new Set<number>([Number(row.host_id), ...participantIds]);
      for (const userId of allUserIds) {
        sendNotification(
          userId,
          'match_completed',
          'Match Completed!',
          'Don\'t forget to rate the players! You have 24 hours.',
          { matchId: String(matchId) }
        ).catch(() => {});
      }
    }
  } catch (e) {
    console.error('Auto-complete past matches failed:', e);
  }
}

// ============================================================
// Handle match-full side effects
// ============================================================

/**
 * Called when a match reaches its maxPlayers capacity.
 * Sends lobby-full notifications, creates split payment shares or
 * links booking participants for cash_at_venue matches, and syncs
 * the match to Firestore.
 *
 * This is a fire-and-forget helper — it never throws.
 */
async function handleMatchFull(match: matchRepo.MatchRow): Promise<void> {
  try {
    const approvedParticipants = match.participants
      .filter((p) => p.status === 'approved');
    const allUserIds = approvedParticipants.map((p) => p.userId);

    // Notify all approved participants that the lobby is full
    notificationService.notifyMatchLobbyFull(
      match.id,
      match.title,
      allUserIds
    ).catch((err) => console.error('Failed to send match-full notification:', err));

    // When match fills and has split payment, auto-create split shares
    if (match.paymentType === 'split' && match.bookingId) {
      try {
        const shareAmount = Math.round((match.totalPrice / approvedParticipants.length) * 100) / 100;

        for (const p of approvedParticipants) {
          // Create split payment share
          const share = await splitRepo.createShare(
            match.bookingId, p.userId, shareAmount, match.currency
          );
          // Link as booking participant
          await participantRepo.inviteMany(match.bookingId, [p.userId]).catch(() => {});
          await participantRepo.linkSplitPayment(match.bookingId, p.userId, share.id);

          // Notify each participant to pay their share
          notificationService.sendNotification(
            p.userId,
            'split_payment_request',
            'Pay Your Share',
            `Match "${match.title}" is full! Pay your share of ${shareAmount} ${match.currency}`,
            { matchId: String(match.id), bookingId: String(match.bookingId), shareAmount: String(shareAmount) }
          ).catch(() => {});
        }
      } catch (err) {
        console.error('Failed to create split payments for match:', err);
      }
    }

    // For cash_at_venue, link all approved participants to the booking
    if (match.paymentType === 'cash_at_venue' && match.bookingId) {
      try {
        for (const p of approvedParticipants) {
          await participantRepo.inviteMany(match.bookingId, [p.userId]).catch(() => {});
        }
      } catch (err) {
        console.error('Failed to link booking participants for cash match:', err);
      }
    }

    // Sync updated match state to Firestore
    await syncMatchToFirestore(match.id);
  } catch (err) {
    console.error('handleMatchFull failed:', err);
  }
}

// ============================================================
// Matches
// ============================================================

export async function createMatch(req: Request, res: Response, next: NextFunction) {
  try {
    const { timeSlotId, venueId } = req.body;
    const paymentType: string = req.body.paymentType || 'host_pays';

    let bookingId: number | undefined;
    let totalPrice = 0;
    let pricePerPlayer = 0;

    // If both venueId and timeSlotId are provided, auto-create a pending booking
    if (venueId && timeSlotId) {
      // Validate the time slot belongs to the venue and is available
      const slot = await timeSlotRepo.findById(timeSlotId);
      if (!slot) throw new NotFoundError('Time slot');
      if (slot.venueId !== venueId) {
        throw new ValidationError('Time slot does not belong to the selected venue');
      }
      if (!slot.isAvailable) {
        throw new ValidationError('Time slot is no longer available');
      }

      // Look up venue to compute pricing
      const venue = await venueRepo.findById(venueId);
      if (venue) {
        totalPrice = slot.priceOverride ?? venue.pricePerHour;
        pricePerPlayer = Math.round((totalPrice / (req.body.maxPlayers || 2)) * 100) / 100;
      }

      // Create the booking via the stored procedure (handles locking, pricing, discounts)
      const booking = await bookingRepo.create(req.user!.id, timeSlotId);
      bookingId = booking.id;

      // Find the venue owner and send a notification
      const ownerResult = await query(
        'SELECT owner_id FROM venues WHERE id = $1',
        [venueId]
      );
      if (ownerResult.rows.length > 0) {
        const ownerId = Number(ownerResult.rows[0].owner_id);
        sendNotification(
          ownerId,
          'booking_request',
          'New Reservation Request',
          `A match lobby wants to book your venue for ${slot.slotDate} ${slot.startTime}-${slot.endTime}`,
          { bookingId: String(bookingId) }
        ).catch((err) => console.error('Failed to send booking notification:', err));
      }
    }

    const match = await matchRepo.create(req.user!.id, {
      ...req.body,
      bookingId,
      paymentType,
      timeSlotId: timeSlotId || null,
      totalPrice,
      pricePerPlayer,
      currency: 'MKD',
    });

    // If a booking was created, link the match back by updating booking_id
    if (bookingId) {
      await query(
        'UPDATE matches SET booking_id = $1, updated_at = NOW() WHERE id = $2',
        [bookingId, match.id]
      );
      match.bookingId = bookingId;
    }

    // If partyId is provided, auto-add all accepted party members
    const { partyId } = req.body;
    if (partyId) {
      const party = await partyRepo.findById(partyId);
      if (party && party.leaderId === req.user!.id) {
        const acceptedMembers = party.members.filter(
          (m) => m.status === 'accepted' && m.userId !== req.user!.id
        );
        const spotsAvailable = match.maxPlayers - 1; // host already counted
        const membersToAdd = acceptedMembers.slice(0, spotsAvailable);

        for (const member of membersToAdd) {
          await matchRepo.addParticipant(match.id, member.userId, true);
        }

        // Update party status to in_match
        await partyRepo.setMatchId(partyId, match.id);

        // Notify party members
        const memberUserIds = membersToAdd.map((m) => m.userId);
        if (memberUserIds.length > 0) {
          notificationService.notifyPartyJoinedMatch(
            memberUserIds,
            match.title,
            req.user!.id,
            partyId,
            match.id
          ).catch((err) => console.error('Failed to send party joined match notifications:', err));
        }
      }
    }

    syncMatchToFirestore(match.id).catch(() => {});
    created(res, match, 'Match created');
  } catch (e) { next(e); }
}

export async function getVenueTimeSlots(req: Request, res: Response, next: NextFunction) {
  try {
    const venueId = Number(req.params.venueId);
    const date = req.query.date ? String(req.query.date) : undefined;
    if (!date) {
      throw new ValidationError('date query parameter is required');
    }

    const slots = await timeSlotRepo.findByVenue(venueId, date, date);
    success(res, slots);
  } catch (e) { next(e); }
}

export async function listMatches(req: Request, res: Response, next: NextFunction) {
  try {
    await autoCompletePastMatches();
    const filters = {
      sportType: req.query.sportType ? String(req.query.sportType) : undefined,
      status: req.query.status ? String(req.query.status) : undefined,
      minSkillLevel: req.query.minSkillLevel ? Number(req.query.minSkillLevel) : undefined,
      maxSkillLevel: req.query.maxSkillLevel ? Number(req.query.maxSkillLevel) : undefined,
      matchDate: req.query.matchDate ? String(req.query.matchDate) : undefined,
      matchType: req.query.matchType ? String(req.query.matchType) : undefined,
      hostId: req.query.hostId ? Number(req.query.hostId) : undefined,
    };
    const matches = await matchRepo.findAll(filters);
    success(res, matches);
  } catch (e) { next(e); }
}

export async function getMyMatches(req: Request, res: Response, next: NextFunction) {
  try {
    await autoCompletePastMatches();
    const [hosted, participating] = await Promise.all([
      matchRepo.findByHostId(req.user!.id),
      matchRepo.findByParticipantId(req.user!.id),
    ]);
    // Merge and deduplicate
    const allIds = new Set<number>();
    const all: matchRepo.MatchRow[] = [];
    for (const m of [...hosted, ...participating]) {
      if (!allIds.has(m.id)) { allIds.add(m.id); all.push(m); }
    }
    all.sort((a, b) => b.matchDate.localeCompare(a.matchDate));
    success(res, all);
  } catch (e) { next(e); }
}

export async function getNearbyMatches(req: Request, res: Response, next: NextFunction) {
  try {
    const lat = Number(req.query.lat);
    const lng = Number(req.query.lng);
    const radius = Number(req.query.radius) || 25;
    const matches = await matchRepo.findNearby(lat, lng, radius);
    success(res, matches);
  } catch (e) { next(e); }
}

export async function getMatchById(req: Request, res: Response, next: NextFunction) {
  try {
    await autoCompletePastMatches();
    const match = await matchRepo.findById(Number(req.params.id));
    if (!match) throw new NotFoundError('Match');
    success(res, match);
  } catch (e) { next(e); }
}

export async function updateMatch(req: Request, res: Response, next: NextFunction) {
  try {
    const match = await matchRepo.findById(Number(req.params.id));
    if (!match) throw new NotFoundError('Match');
    if (match.hostId !== req.user!.id) {
      throw new ForbiddenError('Only the host can update this match');
    }
    const updated = await matchRepo.update(Number(req.params.id), req.body);
    syncMatchToFirestore(Number(req.params.id)).catch(() => {});
    success(res, updated);
  } catch (e) { next(e); }
}

export async function cancelMatch(req: Request, res: Response, next: NextFunction) {
  try {
    const match = await matchRepo.findById(Number(req.params.id));
    if (!match) throw new NotFoundError('Match');
    if (match.hostId !== req.user!.id) {
      throw new ForbiddenError('Only the host can cancel this match');
    }
    const updated = await matchRepo.updateStatus(Number(req.params.id), 'cancelled');
    syncMatchToFirestore(Number(req.params.id)).catch(() => {});
    success(res, updated);
  } catch (e) { next(e); }
}

// ============================================================
// Participants
// ============================================================

export async function joinMatch(req: Request, res: Response, next: NextFunction) {
  try {
    const match = await matchRepo.findById(Number(req.params.id));
    if (!match) throw new NotFoundError('Match');
    // Public matches auto-approve; private matches go to pending
    const autoApprove = match.visibility === 'public';
    const participant = await matchRepo.addParticipant(Number(req.params.id), req.user!.id, autoApprove);

    // Only notify the host when there's actually something to approve.
    // For auto-approved (public) matches we skip the noise.
    if (!autoApprove) {
      const joiner = await userRepo.findById(req.user!.id);
      notificationService.notifyMatchJoinRequest(
        match.id,
        match.hostId,
        joiner?.displayName ?? joiner?.email ?? 'Someone',
        participant.id
      ).catch((err) => console.error('Failed to send join notification:', err));
    }

    // If the match just became full, handle all full-match side effects
    if (autoApprove && match.currentPlayers + 1 >= match.maxPlayers) {
      const updatedMatch = await matchRepo.findById(match.id);
      if (updatedMatch && updatedMatch.currentPlayers >= updatedMatch.maxPlayers) {
        await handleMatchFull(updatedMatch);
      }
    } else {
      syncMatchToFirestore(Number(req.params.id)).catch(() => {});
    }
    created(res, participant, autoApprove ? 'Joined match' : 'Join request sent');
  } catch (e) { next(e); }
}

export async function leaveMatch(req: Request, res: Response, next: NextFunction) {
  try {
    await matchRepo.removeParticipant(Number(req.params.id), req.user!.id);
    syncMatchToFirestore(Number(req.params.id)).catch(() => {});
    success(res, { message: 'Left match' });
  } catch (e) { next(e); }
}

export async function getParticipants(req: Request, res: Response, next: NextFunction) {
  try {
    const participants = await matchRepo.findParticipants(Number(req.params.id));
    success(res, participants);
  } catch (e) { next(e); }
}

export async function respondToJoinRequest(req: Request, res: Response, next: NextFunction) {
  try {
    const match = await matchRepo.findById(Number(req.params.matchId));
    if (!match) throw new NotFoundError('Match');
    if (match.hostId !== req.user!.id) {
      throw new ForbiddenError('Only the host can approve/decline');
    }
    const updated = await matchRepo.updateParticipantStatus(
      Number(req.params.matchId),
      Number(req.params.pid),
      req.body.status
    );
    if (!updated) throw new NotFoundError('Participant');

    // Notify the participant about the host's decision
    if (req.body.status === 'approved') {
      notificationService.notifyMatchJoinApproved(
        match.id,
        updated.userId,
        match.title
      ).catch((err) => console.error('Failed to send approval notification:', err));

      // If the match just became full after this approval, handle all full-match side effects
      const refreshedMatch = await matchRepo.findById(match.id);
      if (refreshedMatch && refreshedMatch.currentPlayers >= refreshedMatch.maxPlayers) {
        await handleMatchFull(refreshedMatch);
      }
    } else if (req.body.status === 'declined') {
      notificationService.notifyMatchJoinDeclined(
        match.id,
        updated.userId,
        match.title
      ).catch((err) => console.error('Failed to send decline notification:', err));
    }

    syncMatchToFirestore(Number(req.params.matchId)).catch(() => {});
    success(res, updated);
  } catch (e) { next(e); }
}

// ============================================================
// Chat
// ============================================================

export async function getChatMessages(req: Request, res: Response, next: NextFunction) {
  try {
    const since = req.query.since ? String(req.query.since) : undefined;
    const limit = req.query.limit ? Number(req.query.limit) : 50;
    const messages = await matchRepo.findChatMessages(Number(req.params.id), since, limit);
    success(res, messages);
  } catch (e) { next(e); }
}

export async function sendChatMessage(req: Request, res: Response, next: NextFunction) {
  try {
    // Accept both `message` (preferred) and `content` (legacy) for backward compatibility
    const text = req.body.message || req.body.content;
    const message = await matchRepo.addChatMessage(
      Number(req.params.id),
      req.user!.id,
      text
    );
    created(res, message, 'Message sent');
  } catch (e) { next(e); }
}

// ============================================================
// Ratings
// ============================================================

export async function ratePlayer(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.matchId);

    // Guard: match must exist and be completed
    const match = await matchRepo.findById(matchId);
    if (!match || match.status !== 'completed') {
      throw new ValidationError('Match must be completed before rating');
    }

    // Guard: 24-hour rating window
    const completedAt = new Date(match.updatedAt);
    const hoursSince = (Date.now() - completedAt.getTime()) / (1000 * 60 * 60);
    if (hoursSince > 24) {
      throw new ValidationError('Rating window has expired (24 hours after match completion)');
    }

    // Guard: rater must be a participant with approved status
    const isParticipant = match.participants.some(
      (p) => p.userId === req.user!.id && p.status === 'approved'
    );
    if (!isParticipant) {
      throw new ForbiddenError('Only approved participants can rate players in this match');
    }

    const rating = await matchRepo.createPlayerRating({
      matchId,
      raterId: req.user!.id,
      ratedId: req.body.ratedId,
      skillRating: req.body.skillRating,
      sportsmanshipRating: req.body.sportsmanshipRating,
      punctualityRating: req.body.punctualityRating,
      comment: req.body.comment,
    });
    created(res, rating, 'Rating submitted');
  } catch (e) { next(e); }
}

export async function getMatchRatings(req: Request, res: Response, next: NextFunction) {
  try {
    const ratings = await matchRepo.findRatingsByMatch(Number(req.params.matchId));
    success(res, ratings);
  } catch (e) { next(e); }
}

export async function getPlayerRatings(req: Request, res: Response, next: NextFunction) {
  try {
    const ratings = await matchRepo.findRatingsByRatedUser(Number(req.params.userId));
    success(res, ratings);
  } catch (e) { next(e); }
}

// ============================================================
// Recurrence Rules
// ============================================================

export async function createRecurrenceRule(req: Request, res: Response, next: NextFunction) {
  try {
    const rule = await matchRepo.createRecurrenceRule(req.user!.id, req.body);
    created(res, rule, 'Recurrence rule created');
  } catch (e) { next(e); }
}

export async function getRecurrenceRule(req: Request, res: Response, next: NextFunction) {
  try {
    const rule = await matchRepo.findRecurrenceRuleById(Number(req.params.id));
    if (!rule) throw new NotFoundError('Recurrence rule');
    success(res, rule);
  } catch (e) { next(e); }
}

export async function updateRecurrenceRule(req: Request, res: Response, next: NextFunction) {
  try {
    const rule = await matchRepo.updateRecurrenceRule(Number(req.params.id), req.body);
    if (!rule) throw new NotFoundError('Recurrence rule');
    success(res, rule);
  } catch (e) { next(e); }
}

export async function deactivateRecurrenceRule(req: Request, res: Response, next: NextFunction) {
  try {
    const rule = await matchRepo.deactivateRecurrenceRule(Number(req.params.id));
    if (!rule) throw new NotFoundError('Recurrence rule');
    success(res, rule);
  } catch (e) { next(e); }
}

export async function generateRecurrences(_req: Request, res: Response, next: NextFunction) {
  try {
    const rules = await matchRepo.findActiveRecurrenceRules();
    const generated: matchRepo.MatchRow[] = [];

    for (const rule of rules) {
      // Generate a match for the rule's next occurrence date
      if (!rule.nextOccurrenceDate) continue;
      const match = await matchRepo.create(rule.hostId, {
        sportType: rule.sportType,
        matchType: 'venue_linked',
        title: rule.title,
        matchDate: rule.nextOccurrenceDate,
        startTime: rule.startTime,
        endTime: rule.endTime,
        minPlayers: rule.minPlayers,
        maxPlayers: rule.maxPlayers,
        minSkillLevel: rule.minSkillLevel ?? undefined,
        maxSkillLevel: rule.maxSkillLevel ?? undefined,
        venueId: rule.venueId ?? undefined,
        locationName: rule.locationName ?? undefined,
        address: rule.address ?? undefined,
        latitude: rule.latitude ?? undefined,
        longitude: rule.longitude ?? undefined,
      });
      generated.push(match);

      // Advance next occurrence date
      const nextDate = new Date(rule.nextOccurrenceDate);
      switch (rule.frequency) {
        case 'weekly': nextDate.setDate(nextDate.getDate() + 7); break;
        case 'biweekly': nextDate.setDate(nextDate.getDate() + 14); break;
        case 'monthly': nextDate.setMonth(nextDate.getMonth() + 1); break;
      }
      await matchRepo.updateRecurrenceRule(rule.id, {
        nextOccurrenceDate: nextDate.toISOString().split('T')[0],
      });
    }

    success(res, { generated: generated.length, matches: generated });
  } catch (e) { next(e); }
}

// ============================================================
// Invite Player (from Available Players)
// ============================================================

export async function invitePlayer(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.id);
    const { userId } = req.body;

    if (!userId) {
      throw new ValidationError('userId is required');
    }

    const match = await matchRepo.findById(matchId);
    if (!match) throw new NotFoundError('Match');

    // Only the host can invite players
    if (match.hostId !== req.user!.id) {
      throw new ForbiddenError('Only the host can invite players to this match');
    }

    // Match must be open to accept new players
    if (match.status !== 'open') {
      throw new ValidationError('Match is not open for new players');
    }

    // Check if match is already full
    if (match.currentPlayers >= match.maxPlayers) {
      throw new ValidationError('Match is already full');
    }

    // Add the invited player as a pending participant
    const participant = await matchRepo.addParticipant(matchId, Number(userId), false);

    // Notify the invited player
    notificationService.sendNotification(
      Number(userId),
      'match_invite',
      'You have been invited to a match',
      `You have been invited to join "${match.title}". Tap to view details.`,
      { matchId: String(matchId) }
    ).catch((err) => console.error('Failed to send invite notification:', err));

    created(res, participant, 'Player invited');
  } catch (e) { next(e); }
}

// ============================================================
// Match Payment (split payment flow)
// ============================================================

export async function getMatchPaymentStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.id);
    const match = await matchRepo.findById(matchId);
    if (!match) throw new NotFoundError('Match');
    if (!match.bookingId) {
      return success(res, { matchId, paymentType: match.paymentType, shares: [] });
    }

    const shares = await splitRepo.findByBookingId(match.bookingId);
    const summary = await splitRepo.getSummary(match.bookingId);

    success(res, {
      matchId,
      bookingId: match.bookingId,
      paymentType: match.paymentType,
      totalPrice: match.totalPrice,
      pricePerPlayer: match.pricePerPlayer,
      currency: match.currency,
      ...summary,
      shares,
    });
  } catch (e) { next(e); }
}

export async function createMatchPaymentIntent(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.id);
    const userId = req.user!.id;

    const match = await matchRepo.findById(matchId);
    if (!match) throw new NotFoundError('Match');
    if (!match.bookingId) throw new ValidationError('Match has no linked booking');

    // Verify user is a participant
    const isParticipant = match.participants.some(p => p.userId === userId && p.status === 'approved');
    if (!isParticipant) throw new ForbiddenError('Only match participants can pay');

    // Find their split payment share
    const shares = await splitRepo.findByBookingId(match.bookingId);
    const myShare = shares.find(s => s.payerUserId === userId);
    if (!myShare) throw new NotFoundError('No payment share found for this user');
    if (myShare.status === 'paid') {
      return success(res, { alreadyPaid: true, share: myShare });
    }

    // For dev mode, generate a dev secret
    const devSecret = `dev_secret_match_${matchId}_${userId}_${Date.now()}`;

    success(res, {
      shareId: myShare.id,
      amount: myShare.amount,
      currency: match.currency,
      clientSecret: devSecret,
      bookingId: match.bookingId,
      matchId: match.id,
    });
  } catch (e) { next(e); }
}

export async function confirmMatchPayment(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.id);
    const userId = req.user!.id;

    const match = await matchRepo.findById(matchId);
    if (!match) throw new NotFoundError('Match');
    if (!match.bookingId) throw new ValidationError('Match has no linked booking');

    // Find the user's split share
    const shares = await splitRepo.findByBookingId(match.bookingId);
    const myShare = shares.find(s => s.payerUserId === userId);
    if (!myShare) throw new NotFoundError('No payment share found');
    if (myShare.status === 'paid') {
      return success(res, myShare, 'Already paid');
    }

    // Mark as paid (use dev payment intent ID for dev mode)
    const paymentIntentId = `dev_match_${matchId}_${userId}_${Date.now()}`;
    const updated = await splitRepo.markPaid(myShare.id, paymentIntentId);

    // Check if all shares are now paid
    const updatedShares = await splitRepo.findByBookingId(match.bookingId);
    const allPaid = updatedShares.every(s => s.status === 'paid');

    if (allPaid) {
      // Confirm the booking
      await query(
        `UPDATE bookings SET status = 'confirmed', updated_at = NOW() WHERE id = $1`,
        [match.bookingId]
      );
      // Notify everyone that the match is fully paid and confirmed
      const allUserIds = match.participants
        .filter(p => p.status === 'approved')
        .map(p => p.userId);
      for (const uid of allUserIds) {
        notificationService.sendNotification(
          uid,
          'booking_confirmed',
          'Match Confirmed!',
          `All players have paid. Match "${match.title}" is confirmed!`,
          { matchId: String(matchId), bookingId: String(match.bookingId) }
        ).catch(() => {});
      }
    }

    success(res, updated, 'Payment confirmed');
  } catch (e) { next(e); }
}
