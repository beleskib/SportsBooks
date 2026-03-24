import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as bookingChatController from '../controllers/bookingChat.controller';

const router = Router();
router.get('/:id/messages', authenticate, bookingChatController.getMessages);
router.post('/:id/messages', authenticate, bookingChatController.sendMessage);
router.get('/:id/contact', authenticate, bookingChatController.getContactInfo);
export default router;
