import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as matchRepo from '../repositories/match.repository';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import * as notificationService from '../services/notification.service';

// ============================================================
// Matches
// ============================================================

export async function createMatch(req: Request, res: Response, next: NextFunction) {
  try {
    const match = await matchRepo.create(req.user!.id, req.body);
    created(res, match, 'Match created');
  } catch (e) { next(e); }
}

export async function listMatches(req: Request, res: Response, next: NextFunction) {
  try {
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

    // Notify the host about the join request
    notificationService.notifyMatchJoinRequest(
      match.id,
      match.hostId,
      req.user!.email
    ).catch((err) => console.error('Failed to send join notification:', err));

    created(res, participant, autoApprove ? 'Joined match' : 'Join request sent');
  } catch (e) { next(e); }
}

export async function leaveMatch(req: Request, res: Response, next: NextFunction) {
  try {
    await matchRepo.removeParticipant(Number(req.params.id), req.user!.id);
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
    } else if (req.body.status === 'declined') {
      notificationService.notifyMatchJoinDeclined(
        match.id,
        updated.userId,
        match.title
      ).catch((err) => console.error('Failed to send decline notification:', err));
    }

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
    const message = await matchRepo.addChatMessage(
      Number(req.params.id),
      req.user!.id,
      req.body.content
    );
    created(res, message, 'Message sent');
  } catch (e) { next(e); }
}

// ============================================================
// Ratings
// ============================================================

export async function ratePlayer(req: Request, res: Response, next: NextFunction) {
  try {
    const rating = await matchRepo.createPlayerRating({
      matchId: Number(req.params.matchId),
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
