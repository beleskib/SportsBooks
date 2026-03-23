import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as favoriteRepo from '../repositories/favorite.repository';

export async function getMyFavorites(req: Request, res: Response, next: NextFunction) {
  try {
    const entityType = req.query.entityType ? String(req.query.entityType) : undefined;
    const favorites = await favoriteRepo.findByUserId(req.user!.id, entityType);
    success(res, favorites);
  } catch (e) { next(e); }
}

export async function toggleFavorite(req: Request, res: Response, next: NextFunction) {
  try {
    const { entityType, entityId } = req.body;
    const result = await favoriteRepo.toggleFavorite(req.user!.id, entityType, entityId);
    success(res, { favorited: result.added });
  } catch (e) { next(e); }
}

export async function checkFavorites(req: Request, res: Response, next: NextFunction) {
  try {
    const { entityType, entityIds } = req.body;
    const result = await favoriteRepo.checkBulk(req.user!.id, entityType, entityIds);
    success(res, result);
  } catch (e) { next(e); }
}
