import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as dmController from '../controllers/directMessage.controller';

const router = Router();

// Conversation list (inbox)
router.get('/', authenticate, dmController.getConversations);

// Unread count (badge)
router.get('/unread-count', authenticate, dmController.getUnreadCount);

// Messages in a specific conversation
router.get('/:friendUserId/messages', authenticate, dmController.getConversation);
router.post('/:friendUserId/messages', authenticate, dmController.sendMessage);

// Mark conversation as read
router.put('/:friendUserId/read', authenticate, dmController.markRead);

export default router;
