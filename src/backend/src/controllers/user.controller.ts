import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as userRepo from '../repositories/user.repository';
import * as feedRepo from '../repositories/feed.repository';
import { NotFoundError } from '../utils/errors';

export async function getMe(req: Request, res: Response, next: NextFunction) {
  try {
    const user = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
    if (!user) throw new NotFoundError('User');
    const interestedSports = await userRepo.findInterestedSports(user.id);
    const sportExpertise = await userRepo.findSportExpertise(user.id);
    success(res, { ...user, interestedSports, sportExpertise });
  } catch (e) { next(e); }
}

export async function updateMe(req: Request, res: Response, next: NextFunction) {
  try {
    let userId = req.user!.id;
    if (!userId) {
      const found = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
      if (!found) throw new NotFoundError('User');
      userId = found.id;
    }
    const user = await userRepo.update(userId, req.body);
    const interestedSports = await userRepo.findInterestedSports(userId);
    const sportExpertise = await userRepo.findSportExpertise(userId);
    success(res, { ...user, interestedSports, sportExpertise });
  } catch (e) { next(e); }
}

export async function setRole(req: Request, res: Response, next: NextFunction) {
  try {
    const { role, partnerType } = req.body;
    const user = await userRepo.setRoleByFirebaseUid(req.user!.firebaseUid, role, partnerType);
    if (!user) throw new NotFoundError('User');
    success(res, user);
  } catch (e) { next(e); }
}

export async function updateInterestedSports(req: Request, res: Response, next: NextFunction) {
  try {
    let userId = req.user!.id;
    if (!userId) {
      const found = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
      if (!found) throw new NotFoundError('User');
      userId = found.id;
    }
    const { sportTypes } = req.body;
    const interestedSports = await userRepo.setInterestedSports(userId, sportTypes || []);
    success(res, { interestedSports });
  } catch (e) { next(e); }
}

export async function completeOnboarding(req: Request, res: Response, next: NextFunction) {
  try {
    let userId = req.user!.id;
    if (!userId) {
      const found = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
      if (!found) throw new NotFoundError('User');
      userId = found.id;
    }
    const { displayName, photoUrl, dateOfBirth, bio, interestedSports, expertise } = req.body;

    // Update profile fields
    await userRepo.update(userId, { displayName, photoUrl, dateOfBirth, bio });

    // Set interested sports
    if (interestedSports && interestedSports.length > 0) {
      await userRepo.setInterestedSports(userId, interestedSports);
    }

    // Set sport expertise
    if (expertise && expertise.length > 0) {
      await userRepo.setSportExpertise(userId, expertise);
    }

    // Mark onboarding as completed
    const user = await userRepo.setOnboardingCompleted(userId);
    const savedSports = await userRepo.findInterestedSports(userId);
    const savedExpertise = await userRepo.findSportExpertise(userId);
    success(res, { ...user, interestedSports: savedSports, sportExpertise: savedExpertise });
  } catch (e) { next(e); }
}

export async function getSportExpertise(req: Request, res: Response, next: NextFunction) {
  try {
    let userId = req.user!.id;
    if (!userId) {
      const found = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
      if (!found) throw new NotFoundError('User');
      userId = found.id;
    }
    const sportExpertise = await userRepo.findSportExpertise(userId);
    success(res, { sportExpertise });
  } catch (e) { next(e); }
}

export async function setSportExpertise(req: Request, res: Response, next: NextFunction) {
  try {
    let userId = req.user!.id;
    if (!userId) {
      const found = await userRepo.findByFirebaseUid(req.user!.firebaseUid);
      if (!found) throw new NotFoundError('User');
      userId = found.id;
    }
    const { expertise } = req.body;
    const sportExpertise = await userRepo.setSportExpertise(userId, expertise || []);
    success(res, { sportExpertise });
  } catch (e) { next(e); }
}

export async function getFollowCounts(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const [followers, following] = await Promise.all([
      feedRepo.getFollowerCount(userId),
      feedRepo.getFollowingCount(userId),
    ]);
    success(res, { followers, following });
  } catch (e) { next(e); }
}
