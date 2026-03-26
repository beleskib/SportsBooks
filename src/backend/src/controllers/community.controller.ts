import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as communityRepo from '../repositories/community.repository';
import { NotFoundError, ForbiddenError, ValidationError, ConflictError } from '../utils/errors';
import { sendNotification } from '../services/notification.service';

// ============================================================
// Communities
// ============================================================

export async function createCommunity(req: Request, res: Response, next: NextFunction) {
  try {
    const community = await communityRepo.create(req.user!.id, req.body);
    created(res, community, 'Community created');
  } catch (e) { next(e); }
}

export async function listMyCommunities(req: Request, res: Response, next: NextFunction) {
  try {
    const communities = await communityRepo.findByUserId(req.user!.id);
    success(res, communities);
  } catch (e) { next(e); }
}

export async function listPublicCommunities(req: Request, res: Response, next: NextFunction) {
  try {
    const sportType = req.query.sportType ? String(req.query.sportType) : undefined;
    const communities = await communityRepo.findPublic(sportType);
    success(res, communities);
  } catch (e) { next(e); }
}

export async function getCommunityById(req: Request, res: Response, next: NextFunction) {
  try {
    const community = await communityRepo.findById(Number(req.params.id));
    if (!community) throw new NotFoundError('Community');
    success(res, community);
  } catch (e) { next(e); }
}

export async function updateCommunity(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    const isAdminOrOwner = await communityRepo.isAdminOrOwner(communityId, req.user!.id);
    if (!isAdminOrOwner) {
      throw new ForbiddenError('Only the owner or an admin can update this community');
    }

    const updated = await communityRepo.update(communityId, req.body);
    success(res, updated);
  } catch (e) { next(e); }
}

// ============================================================
// Invites & Join Requests
// ============================================================

export async function inviteUsers(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const { userIds } = req.body;

    if (!Array.isArray(userIds) || userIds.length === 0) {
      throw new ValidationError('userIds must be a non-empty array');
    }

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    // Inviter must be an approved member
    const inviterIsMember = await communityRepo.isMember(communityId, req.user!.id);
    if (!inviterIsMember) {
      throw new ForbiddenError('You must be a member of this community to invite others');
    }

    const results: { userId: number; status: string; reason?: string }[] = [];

    for (const userId of userIds) {
      const targetUserId = Number(userId);

      // Check if already a member
      const alreadyMember = await communityRepo.isMember(communityId, targetUserId);
      if (alreadyMember) {
        results.push({ userId: targetUserId, status: 'skipped', reason: 'already a member' });
        continue;
      }

      // Enforce invite policy
      if (community.invitePolicy === 'friends_only') {
        const friends = await communityRepo.areFriends(req.user!.id, targetUserId);
        if (!friends) {
          results.push({ userId: targetUserId, status: 'skipped', reason: 'not a friend' });
          continue;
        }
      } else if (community.invitePolicy === 'friends_of_friends') {
        const friends = await communityRepo.areFriends(req.user!.id, targetUserId);
        const friendsOfFriends = friends || await communityRepo.areFriendsOfFriends(req.user!.id, targetUserId);
        if (!friendsOfFriends) {
          results.push({ userId: targetUserId, status: 'skipped', reason: 'not a friend or friend-of-friend' });
          continue;
        }
      }
      // 'open' policy: anyone can be invited

      try {
        await communityRepo.addMember(communityId, targetUserId, req.user!.id, 'pending');
        results.push({ userId: targetUserId, status: 'invited' });

        // Notify the invited user
        sendNotification(
          targetUserId,
          'community_invite',
          'Community Invitation',
          `You have been invited to join "${community.name}". Tap to respond.`,
          { communityId: String(communityId) }
        ).catch((err) => console.error('Failed to send community invite notification:', err));
      } catch (err: any) {
        // Unique constraint violation means already invited/pending
        if (err.code === '23505') {
          results.push({ userId: targetUserId, status: 'skipped', reason: 'already invited or pending' });
        } else {
          throw err;
        }
      }
    }

    success(res, results);
  } catch (e) { next(e); }
}

export async function joinCommunity(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    if (!community.isPublic) {
      throw new ForbiddenError('This community is not public. You need an invitation to join.');
    }

    // Check if already a member
    const alreadyMember = await communityRepo.isMember(communityId, req.user!.id);
    if (alreadyMember) {
      throw new ConflictError('You are already a member of this community');
    }

    // For public communities, auto-approve
    const member = await communityRepo.addMember(communityId, req.user!.id, null, 'approved');
    created(res, member, 'Joined community');
  } catch (e) { next(e); }
}

export async function respondToMember(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const targetUserId = Number(req.params.userId);
    const { status } = req.body;

    if (!status || !['approved', 'declined'].includes(status)) {
      throw new ValidationError('status must be "approved" or "declined"');
    }

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    const isAdminOrOwner = await communityRepo.isAdminOrOwner(communityId, req.user!.id);
    if (!isAdminOrOwner) {
      throw new ForbiddenError('Only the owner or an admin can approve/decline members');
    }

    const updated = await communityRepo.updateMemberStatus(communityId, targetUserId, status);
    if (!updated) throw new NotFoundError('Community member');

    // Notify the user if approved
    if (status === 'approved') {
      sendNotification(
        targetUserId,
        'community_member_approved',
        'Welcome to the community!',
        `Your request to join "${community.name}" has been approved.`,
        { communityId: String(communityId) }
      ).catch((err) => console.error('Failed to send approval notification:', err));
    }

    success(res, updated);
  } catch (e) { next(e); }
}

export async function changeMemberRole(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const targetUserId = Number(req.params.userId);
    const { role } = req.body;

    if (!role || !['admin', 'member'].includes(role)) {
      throw new ValidationError('role must be "admin" or "member"');
    }

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    // Only owner can change roles
    const ownerCheck = await communityRepo.isOwner(communityId, req.user!.id);
    if (!ownerCheck) {
      throw new ForbiddenError('Only the owner can change member roles');
    }

    const updated = await communityRepo.updateMemberRole(communityId, targetUserId, role);
    if (!updated) throw new NotFoundError('Community member');

    success(res, updated);
  } catch (e) { next(e); }
}

export async function removeMember(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const targetUserId = Number(req.params.userId);

    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    const isSelf = req.user!.id === targetUserId;

    if (isSelf) {
      // Cannot remove yourself if you are the owner
      const ownerCheck = await communityRepo.isOwner(communityId, req.user!.id);
      if (ownerCheck) {
        throw new ForbiddenError('The owner cannot leave the community. Transfer ownership first.');
      }
    } else {
      // Must be admin/owner to remove others
      const isAdminOrOwner = await communityRepo.isAdminOrOwner(communityId, req.user!.id);
      if (!isAdminOrOwner) {
        throw new ForbiddenError('Only the owner or an admin can remove members');
      }

      // Admins cannot remove owners
      const targetIsOwner = await communityRepo.isOwner(communityId, targetUserId);
      if (targetIsOwner) {
        throw new ForbiddenError('Cannot remove the owner from the community');
      }
    }

    const removed = await communityRepo.removeMember(communityId, targetUserId);
    if (!removed) throw new NotFoundError('Community member');

    success(res, { message: isSelf ? 'Left community' : 'Member removed' });
  } catch (e) { next(e); }
}

export async function listMembers(req: Request, res: Response, next: NextFunction) {
  try {
    const communityId = Number(req.params.id);
    const community = await communityRepo.findById(communityId);
    if (!community) throw new NotFoundError('Community');

    const status = req.query.status ? String(req.query.status) : undefined;
    const members = await communityRepo.getMembers(communityId, status);
    success(res, members);
  } catch (e) { next(e); }
}
