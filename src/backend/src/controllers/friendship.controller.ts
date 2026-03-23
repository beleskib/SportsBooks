import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ConflictError, NotFoundError, ValidationError } from '../utils/errors';
import * as friendshipRepo from '../repositories/friendship.repository';
import * as userRepo from '../repositories/user.repository';
import * as notificationService from '../services/notification.service';

export async function getMyFriends(req: Request, res: Response, next: NextFunction) {
  try {
    const friends = await friendshipRepo.findFriends(req.user!.id);
    success(res, friends);
  } catch (e) { next(e); }
}

export async function getPendingRequests(req: Request, res: Response, next: NextFunction) {
  try {
    const requests = await friendshipRepo.findPendingRequests(req.user!.id);
    success(res, requests);
  } catch (e) { next(e); }
}

export async function sendFriendRequest(req: Request, res: Response, next: NextFunction) {
  try {
    const { userId } = req.body;
    if (!userId) {
      throw new ValidationError('userId is required');
    }

    if (userId === req.user!.id) {
      throw new ValidationError('You cannot send a friend request to yourself');
    }

    // Check that the target user exists
    const targetUser = await userRepo.findById(userId);
    if (!targetUser) {
      throw new NotFoundError('User');
    }

    // Check no existing friendship in either direction
    const existing = await friendshipRepo.findFriendship(req.user!.id, userId);
    if (existing) {
      throw new ConflictError(`Friendship already exists with status: ${existing.status}`);
    }

    const friendship = await friendshipRepo.sendRequest(req.user!.id, userId);

    // Send notification to the addressee (fire-and-forget)
    const requesterName = req.user!.email; // fallback; ideally display_name
    notificationService.notifyFriendRequest(
      userId,
      requesterName
    ).catch((err) => console.error('Failed to send friend request notification:', err));

    created(res, friendship, 'Friend request sent');
  } catch (e) { next(e); }
}

export async function respondToFriendRequest(req: Request, res: Response, next: NextFunction) {
  try {
    const friendshipId = Number(req.params.id);
    const { accept } = req.body;

    if (typeof accept !== 'boolean') {
      throw new ValidationError('accept must be a boolean');
    }

    const updated = await friendshipRepo.respondToRequest(friendshipId, req.user!.id, accept);
    if (!updated) {
      throw new NotFoundError('Friend request');
    }

    // If accepted, notify the requester (fire-and-forget)
    if (accept) {
      const friendName = req.user!.email; // fallback; ideally display_name
      notificationService.notifyFriendRequestAccepted(
        updated.requesterId,
        friendName
      ).catch((err) => console.error('Failed to send friend accepted notification:', err));
    }

    success(res, updated, accept ? 'Friend request accepted' : 'Friend request declined');
  } catch (e) { next(e); }
}

export async function removeFriend(req: Request, res: Response, next: NextFunction) {
  try {
    const friendId = Number(req.params.friendId);
    const removed = await friendshipRepo.removeFriend(req.user!.id, friendId);
    if (!removed) {
      throw new NotFoundError('Friendship');
    }
    success(res, { message: 'Friend removed' });
  } catch (e) { next(e); }
}

export async function searchUsers(req: Request, res: Response, next: NextFunction) {
  try {
    const q = req.query.q ? String(req.query.q) : '';
    if (!q.trim()) {
      success(res, []);
      return;
    }
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const users = await friendshipRepo.searchUsers(q, req.user!.id, limit);
    success(res, users);
  } catch (e) { next(e); }
}
