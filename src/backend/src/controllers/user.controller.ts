import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as userRepo from '../repositories/user.repository';
import { NotFoundError } from '../utils/errors';

export async function getMe(req: Request, res: Response, next: NextFunction) {
  try {
    const user = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
    if (!user) throw new NotFoundError('User');
    success(res, user);
  } catch (e) { next(e); }
}

export async function updateMe(req: Request, res: Response, next: NextFunction) {
  try {
    const user = await userRepo.update(req.user!.id, req.body);
    success(res, user);
  } catch (e) { next(e); }
}

export async function setRole(req: Request, res: Response, next: NextFunction) {
  try {
    const { role, partnerType } = req.body;
    const user = await userRepo.setRole(req.user!.id, role, partnerType);
    success(res, user);
  } catch (e) { next(e); }
}
