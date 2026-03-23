import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as availablePlayerController from '../controllers/availablePlayer.controller';

const router = Router();

router.post('/', authenticate, availablePlayerController.register);
router.get('/me', authenticate, availablePlayerController.getMyAvailability);
router.get('/', authenticate, availablePlayerController.listAvailable);
router.delete('/:sportType', authenticate, availablePlayerController.unregister);

export default router;
