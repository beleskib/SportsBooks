import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as userController from '../controllers/user.controller';

const router = Router();
router.get('/me', authenticate, userController.getMe);
router.put('/me', authenticate, userController.updateMe);
router.put('/me/role', authenticate, userController.setRole);
router.put('/me/interested-sports', authenticate, userController.updateInterestedSports);
router.post('/me/complete-onboarding', authenticate, userController.completeOnboarding);
router.get('/me/sport-expertise', authenticate, userController.getSportExpertise);
router.put('/me/sport-expertise', authenticate, userController.setSportExpertise);
export default router;
