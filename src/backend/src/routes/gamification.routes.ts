import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as gamificationController from '../controllers/gamification.controller';

const router = Router();
router.get('/me/level', authenticate, gamificationController.getMyLevel);
router.get('/me/xp-history', authenticate, gamificationController.getMyXpHistory);
router.get('/achievements', authenticate, gamificationController.getAchievements);
router.get('/me/achievements', authenticate, gamificationController.getMyAchievements);
router.get('/me/stats', authenticate, gamificationController.getMyStats);
router.post('/me/check-achievements', authenticate, gamificationController.checkAndAwardAchievements);
export default router;
