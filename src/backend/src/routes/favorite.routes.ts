import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as favoriteController from '../controllers/favorite.controller';

const router = Router();
router.get('/', authenticate, favoriteController.getMyFavorites);
router.post('/toggle', authenticate, favoriteController.toggleFavorite);
router.post('/check', authenticate, favoriteController.checkFavorites);
export default router;
