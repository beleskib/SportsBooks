import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as notificationController from '../controllers/notification.controller';

const router = Router();
router.get('/', authenticate, notificationController.getNotifications);
router.get('/unread-count', authenticate, notificationController.getUnreadCount);
router.post('/mark-read', authenticate, notificationController.markAsRead);
router.post('/device-token', authenticate, notificationController.registerDeviceToken);
router.delete('/device-token', authenticate, notificationController.removeDeviceToken);
export default router;
