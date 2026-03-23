import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ConflictError, ForbiddenError, NotFoundError, ValidationError } from '../utils/errors';
import * as partyRepo from '../repositories/party.repository';
import * as matchRepo from '../repositories/match.repository';
import * as friendshipRepo from '../repositories/friendship.repository';
import * as notificationService from '../services/notification.service';

export async function createParty(req: Request, res: Response, next: NextFunction) {
  try {
    const { name, sportType } = req.body;

    // Check user has no active party
    const activeParty = await partyRepo.findActiveByUserId(req.user!.id);
    if (activeParty) {
      throw new ConflictError('You already have an active party. Disband it before creating a new one.');
    }

    const party = await partyRepo.create(req.user!.id, name, sportType);
    created(res, party, 'Party created');
  } catch (e) { next(e); }
}

export async function getActiveParty(req: Request, res: Response, next: NextFunction) {
  try {
    const party = await partyRepo.findActiveByUserId(req.user!.id);
    success(res, party);
  } catch (e) { next(e); }
}

export async function getPartyById(req: Request, res: Response, next: NextFunction) {
  try {
    const party = await partyRepo.findById(Number(req.params.id));
    if (!party) throw new NotFoundError('Party');
    success(res, party);
  } catch (e) { next(e); }
}

export async function inviteToParty(req: Request, res: Response, next: NextFunction) {
  try {
    const partyId = Number(req.params.id);
    const { userIds } = req.body;

    if (!userIds || !Array.isArray(userIds) || userIds.length === 0) {
      throw new ValidationError('userIds must be a non-empty array');
    }

    // Load party and verify user is the leader
    const party = await partyRepo.findById(partyId);
    if (!party) throw new NotFoundError('Party');

    if (party.leaderId !== req.user!.id) {
      throw new ForbiddenError('Only the party leader can invite members');
    }

    if (party.status !== 'forming') {
      throw new ValidationError('Can only invite members while party is forming');
    }

    // Verify each userId is a friend
    for (const userId of userIds) {
      const friendship = await friendshipRepo.findFriendship(req.user!.id, userId);
      if (!friendship || friendship.status !== 'accepted') {
        throw new ValidationError(`User ${userId} is not your friend`);
      }
    }

    // Insert invited members
    await partyRepo.inviteMembers(partyId, userIds);

    // Send party_invite notification to each invited user (fire-and-forget)
    const leaderName = req.user!.email;
    const partyName = party.name || 'a party';
    for (const userId of userIds) {
      notificationService.notifyPartyInvite(
        userId,
        leaderName,
        partyName
      ).catch((err) => console.error('Failed to send party invite notification:', err));
    }

    // Return updated party
    const updatedParty = await partyRepo.findById(partyId);
    success(res, updatedParty);
  } catch (e) { next(e); }
}

export async function respondToInvite(req: Request, res: Response, next: NextFunction) {
  try {
    const partyId = Number(req.params.id);
    const { accept } = req.body;

    if (typeof accept !== 'boolean') {
      throw new ValidationError('accept must be a boolean');
    }

    const updated = await partyRepo.respondToInvite(partyId, req.user!.id, accept);
    if (!updated) {
      throw new NotFoundError('Party invite');
    }

    // Load party for notification context
    const party = await partyRepo.findById(partyId);
    if (party) {
      const memberName = req.user!.email;

      if (accept) {
        // Notify leader that member accepted
        notificationService.notifyPartyInviteAccepted(
          party.leaderId,
          memberName
        ).catch((err) => console.error('Failed to send party invite accepted notification:', err));
      } else {
        // Notify leader that member declined
        notificationService.notifyPartyInviteDeclined(
          party.leaderId,
          memberName
        ).catch((err) => console.error('Failed to send party invite declined notification:', err));
      }

      // Check if party should auto-transition to ready
      await partyRepo.checkAutoReady(partyId);
    }

    // Return updated party
    const updatedParty = await partyRepo.findById(partyId);
    success(res, updatedParty, accept ? 'Invite accepted' : 'Invite declined');
  } catch (e) { next(e); }
}

export async function disbandParty(req: Request, res: Response, next: NextFunction) {
  try {
    const partyId = Number(req.params.id);

    const party = await partyRepo.findById(partyId);
    if (!party) throw new NotFoundError('Party');

    if (party.leaderId !== req.user!.id) {
      throw new ForbiddenError('Only the party leader can disband the party');
    }

    await partyRepo.disbandParty(partyId);

    // Notify all accepted members (fire-and-forget)
    const partyName = party.name || 'your party';
    const acceptedMemberIds = party.members
      .filter((m) => m.status === 'accepted')
      .map((m) => m.userId);

    notificationService.notifyPartyDisbanded(
      acceptedMemberIds,
      partyName,
      req.user!.id
    ).catch((err) => console.error('Failed to send party disbanded notifications:', err));

    success(res, { message: 'Party disbanded' });
  } catch (e) { next(e); }
}

export async function joinMatchWithParty(req: Request, res: Response, next: NextFunction) {
  try {
    const matchId = Number(req.params.id);
    const { partyId } = req.body;

    if (!partyId) {
      throw new ValidationError('partyId is required');
    }

    // 1. Load party, verify user is leader, verify party status is 'ready'
    const party = await partyRepo.findById(partyId);
    if (!party) throw new NotFoundError('Party');

    if (party.leaderId !== req.user!.id) {
      throw new ForbiddenError('Only the party leader can join a match with the party');
    }

    if (party.status !== 'ready') {
      throw new ValidationError('Party must be in "ready" status to join a match');
    }

    // 2. Load match, verify match status is 'open', verify enough spots
    const match = await matchRepo.findById(matchId);
    if (!match) throw new NotFoundError('Match');

    if (match.status !== 'open') {
      throw new ValidationError('Match is not open for joining');
    }

    const acceptedMembers = party.members.filter((m) => m.status === 'accepted');
    const spotsAvailable = match.maxPlayers - match.currentPlayers;

    if (acceptedMembers.length > spotsAvailable) {
      throw new ValidationError(
        `Not enough spots. Party has ${acceptedMembers.length} members but only ${spotsAvailable} spots available.`
      );
    }

    // 3. Add each accepted party member as a participant (auto-approved)
    for (const member of acceptedMembers) {
      await matchRepo.addParticipant(matchId, member.userId, true);
    }

    // 4. Update party: set match_id and status to 'in_match'
    await partyRepo.setMatchId(partyId, matchId);

    // 5. Send party_joined_match notification to all members (fire-and-forget)
    const memberUserIds = acceptedMembers.map((m) => m.userId);
    notificationService.notifyPartyJoinedMatch(
      memberUserIds,
      match.title,
      req.user!.id
    ).catch((err) => console.error('Failed to send party joined match notifications:', err));

    // Return updated match
    const updatedMatch = await matchRepo.findById(matchId);
    success(res, updatedMatch);
  } catch (e) { next(e); }
}
