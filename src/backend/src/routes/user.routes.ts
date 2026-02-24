import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as userController from '../controllers/user.controller';

const router = Router();
router.get('/me', authenticate, userController.getMe);
router.put('/me', authenticate, userController.updateMe);
router.put('/me/role', authenticate, userController.setRole);
export default router;
