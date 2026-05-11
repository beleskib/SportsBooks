import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { registerSchema } from '../schemas/auth.schema';
import * as authController from '../controllers/auth.controller';

const router = Router();
router.post('/register', authenticate, validate(registerSchema), authController.register);
export default router;
