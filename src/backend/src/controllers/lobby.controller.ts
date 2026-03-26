import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as lobbyRepo from '../repositories/lobby.repository';
import * as communityRepo from '../repositories/community.repository';
import { NotFoundError, ForbiddenError, ValidationError, ConflictError } from '../utils/errors';
import { sendNotification } from '../services/notification.service';

// ============================================================
// Lobbies
// ============================================================

export async function createLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.communityId);

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    // Must be a community member to create a lobby
    const isMember = await communityRepo.isMember(communityId, req.user!.id);
    if (!isMember) {
      throw new ForbiddenError('You must be a member of this community to create a lobby');
    }

    const lobby = await lobbyRepo.create(communityId, req.user!.id, req.body);

    // Notify all community members about the new lobby
    const members = await communityRepo.getMembers(communityId, 'approved');
    for (const member of members) {
      if (member.userId === req.user!.id) continue;
      sendNotification(
        member.userId,
        'community_lobby_created',
        'New Lobby',
        `A new lobby "${lobby.title}" was created in "${community.name}". Tap to join.`,
        { communityId: String(communityId), lobbyId: String(lobby.id) }
      ).catch((err) => console.error('Failed to send lobby creation notification:', err));
    }

    created(res, lobby, 'Lobby created');
  } catch (e) { next(e); }
}

export async function listCommunityLobbies(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.communityId);

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    const status = req.query.status ? String(req.query.status) : undefined;
    const lobbies = await lobbyRepo.findByCommunity(communityId, status);
    success(res, lobbies);
  } catch (e) { next(e); }
}

export async function listPublicLobbies(req: Request, res: Response, next: NextFunction) {
  try {
    const sportType = req.query.sportType ? String(req.query.sportType) : undefined;
    const date = req.query.date ? String(req.query.date) : undefined;
    const lobbies = await lobbyRepo.findPublicLobbies(sportType, date);
    success(res, lobbies);
  } catch (e) { next(e); }
}

export async function getLobbyById(req: Request, res: Response, next: NextFunction) {
  try {
    const lobby = await lobbyRepo.findById(Number(req.params.id));
    if (!lobby) throw new NotFoundError('Lobby');
    success(res, lobby);
  } catch (e) { next(e); }
}

export async function joinLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const lobby = await lobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Lobby');

    if (lobby.status !== 'open') {
      throw new ValidationError('This lobby is not open for joining');
    }

    if (lobby.currentPlayers >= lobby.maxPlayers) {
      throw new ValidationError('This lobby is full');
    }

    // Check if already a participant
    const alreadyJoined = await lobbyRepo.isParticipant(lobbyId, req.user!.id);
    if (alreadyJoined) {
      throw new ConflictError('You are already in this lobby');
    }

    // Must be community member OR lobby must be public
    if (!lobby.isPublic) {
      const isMember = await communityRepo.isMember(lobby.communityId, req.user!.id);
      if (!isMember) {
        throw new ForbiddenError('You must be a member of the community to join this lobby');
      }
    }

    const participant = await lobbyRepo.join(lobbyId, req.user!.id);
    created(res, participant, 'Joined lobby');
  } catch (e) { next(e); }
}

export async function leaveLobby(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const lobby = await lobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Lobby');

    // Creator cannot leave their own lobby (they should cancel it)
    if (lobby.createdBy === req.user!.id) {
      throw new ForbiddenError('The lobby creator cannot leave. Cancel the lobby instead.');
    }

    const left = await lobbyRepo.leave(lobbyId, req.user!.id);
    if (!left) throw new NotFoundError('Lobby participant');

    success(res, { message: 'Left lobby' });
  } catch (e) { next(e); }
}

export async function makeLobbyPublic(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const lobby = await lobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Lobby');

    if (lobby.isPublic) {
      throw new ValidationError('This lobby is already public');
    }

    // Only creator or community admin/owner can make public
    const isCreator = lobby.createdBy === req.user!.id;
    const isAdminOrOwner = await communityRepo.isAdminOrOwner(lobby.communityId, req.user!.id);
    if (!isCreator && !isAdminOrOwner) {
      throw new ForbiddenError('Only the lobby creator or a community admin can make this lobby public');
    }

    const updated = await lobbyRepo.makePublic(lobbyId);
    success(res, updated);
  } catch (e) { next(e); }
}

export async function updateLobbyStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const lobbyId = Number(req.params.id);
    const { status } = req.body;

    if (!status || !['open', 'full', 'in_progress', 'completed', 'cancelled'].includes(status)) {
      throw new ValidationError('status must be one of: open, full, in_progress, completed, cancelled');
    }

    const lobby = await lobbyRepo.findById(lobbyId);
    if (!lobby) throw new NotFoundError('Lobby');

    // Only creator or community admin/owner can update status
    const isCreator = lobby.createdBy === req.user!.id;
    const isAdminOrOwner = await communityRepo.isAdminOrOwner(lobby.communityId, req.user!.id);
    if (!isCreator && !isAdminOrOwner) {
      throw new ForbiddenError('Only the lobby creator or a community admin can update the status');
    }

    const updated = await lobbyRepo.updateStatus(lobbyId, status);
    success(res, updated);
  } catch (e) { next(e); }
}
