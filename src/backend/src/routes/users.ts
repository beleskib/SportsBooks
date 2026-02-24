import { Router, Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import * as userRepo from '../repositories/user.repository';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validate';
import { success } from '../utils/apiResponse';
import { NotFoundError } from '../utils/errors';

const router = Router();

const syncSchema = z.object({
  email: z.string().email(),
  displayName: z.string().optional(),
  photoUrl: z.string().url().optional(),
});

const updateSchema = z.object({
  displayName: z.string().optional(),
  photoUrl: z.string().url().optional(),
  phoneNumber: z.string().optional(),
});

// POST /api/users/sync — create or return user record after Firebase sign-in
router.post('/sync', authenticate, validate(syncSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { email, displayName, photoUrl } = req.body;
    const firebaseUid = req.user!.firebaseUid;

    let user = await userRepo.findByFirebaseUid(firebaseUid);
    if (!user) {
      user = await userRepo.create({ firebaseUid, email, displayName, photoUrl });
    }
    success(res, user);
  } catch (err) {
    next(err);
  }
});

// GET /api/users/me
router.get('/me', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const user = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
    if (!user) throw new NotFoundError('User');
    success(res, user);
  } catch (err) {
    next(err);
  }
});

// PUT /api/users/me
router.put('/me', authenticate, validate(updateSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    if (!req.user!.id) throw new NotFoundError('User');
    const user = await userRepo.update(req.user!.id, req.body);
    success(res, user);
  } catch (err) {
    next(err);
  }
});

export default router;
