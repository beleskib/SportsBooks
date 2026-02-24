import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as sportController from '../controllers/sport.controller';

const router = Router();
router.get('/', authenticate, sportController.getAll);
router.get('/:id', authenticate, sportController.getById);
export default router;
