import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as friendshipController from '../controllers/friendship.controller';

const router = Router();
router.get('/', authenticate, friendshipController.getMyFriends);
router.get('/requests', authenticate, friendshipController.getPendingRequests);
router.post('/request', authenticate, friendshipController.sendFriendRequest);
router.put('/request/:id/respond', authenticate, friendshipController.respondToFriendRequest);
router.delete('/:friendId', authenticate, friendshipController.removeFriend);
router.get('/search-users', authenticate, friendshipController.searchUsers);
export default router;
