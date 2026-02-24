import { Request, Response, NextFunction } from 'express';
import { created } from '../utils/apiResponse';
import * as userRepo from '../repositories/user.repository';
import { ConflictError } from '../utils/errors';

export async function register(req: Request, res: Response, next: NextFunction) {
  try {
    const { firebaseUid, email, displayName, photoUrl } = req.body;
    const existing = await userRepo.findByFirebaseUid(firebaseUid || req.user?.firebaseUid);
    if (existing) throw new ConflictError('User already registered');
    const user = await userRepo.create({
      firebaseUid: firebaseUid || req.user!.firebaseUid,
      email: email || req.user!.email,
      displayName, photoUrl,
    });
    created(res, user, 'User registered');
  } catch (e) { next(e); }
}
