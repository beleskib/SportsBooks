import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as authController from '../controllers/auth.controller';

const router = Router();
router.post('/register', authenticate, authController.register);
export default router;
