import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ConflictError, NotFoundError, ValidationError } from '../utils/errors';
import * as friendshipRepo from '../repositories/friendship.repository';
import * as userRepo from '../repositories/user.repository';
import * as notificationService from '../services/notification.service';

export async function getMyFriends(req: Request, res: Response, next: NextFunction) {
  try {
    const friends = await friendshipRepo.findFriends(req.user!.id);
    // Transform to consistent shape Android expects (FriendshipDto with nested user)
    const mapped = friends.map(f => ({
      id: f.friendshipId,
      requesterId: 0,
      addresseeId: 0,
      status: 'accepted',
      user: {
        id: f.userId,
        displayName: f.displayName,
        photoUrl: f.photoUrl,
      },
      createdAt: f.createdAt,
    }));
    success(res, mapped);
  } catch (e) { next(e); }
}

export async function getPendingRequests(req: Request, res: Response, next: NextFunction) {
  try {
    const requests = await friendshipRepo.findPendingRequests(req.user!.id);
    // Transform to consistent shape Android expects
    const mapped = requests.map(r => ({
      id: r.friendshipId,
      requesterId: r.requesterId,
      addresseeId: req.user!.id,
      status: 'pending',
      user: {
        id: r.requesterId,
        displayName: r.displayName,
        photoUrl: r.photoUrl,
      },
      createdAt: r.createdAt,
    }));
    success(res, mapped);
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
    const currentUser = await userRepo.findById(req.user!.id);
    const requesterName = currentUser?.displayName || req.user!.email || 'Someone';
    notificationService.notifyFriendRequest(
      userId,
      requesterName
    ).catch((err) => console.error('Failed to send friend request notification:', err));

    // Return response with target user info so Android gets a complete object
    const response = {
      id: friendship.id,
      requesterId: friendship.requesterId,
      addresseeId: friendship.addresseeId,
      status: friendship.status,
      user: {
        id: targetUser.id,
        displayName: targetUser.displayName,
        photoUrl: targetUser.photoUrl,
      },
      createdAt: friendship.createdAt,
      updatedAt: friendship.updatedAt,
    };

    created(res, response, 'Friend request sent');
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
      // Get the current user's display name for a proper notification
      const currentUser = await userRepo.findById(req.user!.id);
      const friendName = currentUser?.displayName || req.user!.email || 'Someone';
      notificationService.notifyFriendRequestAccepted(
        updated.requesterId,
        friendName
      ).catch((err) => console.error('Failed to send friend accepted notification:', err));
    }

    // Get requester info so Android gets a complete response with user object
    const requester = await userRepo.findById(updated.requesterId);
    const response = {
      id: updated.id,
      requesterId: updated.requesterId,
      addresseeId: updated.addresseeId,
      status: updated.status,
      user: {
        id: updated.requesterId,
        displayName: requester?.displayName || null,
        photoUrl: requester?.photoUrl || null,
      },
      createdAt: updated.createdAt,
      updatedAt: updated.updatedAt,
    };

    success(res, response, accept ? 'Friend request accepted' : 'Friend request declined');
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
