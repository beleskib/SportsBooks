import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as communityController from '../controllers/community.controller';

const router = Router();

// Communities
router.post('/', authenticate, communityController.createCommunity);
router.get('/', authenticate, communityController.listMyCommunities);
router.get('/public', authenticate, communityController.listPublicCommunities);
router.get('/:id', authenticate, communityController.getCommunityById);
router.put('/:id', authenticate, communityController.updateCommunity);

// Invites & Join
router.post('/:id/invite', authenticate, communityController.inviteUsers);
router.post('/:id/join', authenticate, communityController.joinCommunity);

// Member management
router.put('/:id/members/:userId/respond', authenticate, communityController.respondToMember);
router.put('/:id/members/:userId/role', authenticate, communityController.changeMemberRole);
router.delete('/:id/members/:userId', authenticate, communityController.removeMember);
router.get('/:id/members', authenticate, communityController.listMembers);

export default router;
