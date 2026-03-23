import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as availablePlayerRepo from '../repositories/availablePlayer.repository';
import { ValidationError } from '../utils/errors';

export async function register(req: Request, res: Response, next: NextFunction) {
  try {
    const { sportType, skillLevel, note, latitude, longitude, availableUntil } = req.body;
    if (!sportType) {
      throw new ValidationError('sportType is required');
    }
    const row = await availablePlayerRepo.register(
      req.user!.id,
      sportType,
      skillLevel != null ? Number(skillLevel) : undefined,
      note,
      latitude != null ? Number(latitude) : undefined,
      longitude != null ? Number(longitude) : undefined,
      availableUntil
    );
    created(res, row, 'Registered as available');
  } catch (e) { next(e); }
}

export async function unregister(req: Request, res: Response, next: NextFunction) {
  try {
    const { sportType } = req.params;
    if (!sportType) {
      throw new ValidationError('sportType param is required');
    }
    const removed = await availablePlayerRepo.unregister(req.user!.id, sportType as string);
    success(res, { removed }, removed ? 'Availability removed' : 'No availability found for that sport');
  } catch (e) { next(e); }
}

export async function getMyAvailability(req: Request, res: Response, next: NextFunction) {
  try {
    const rows = await availablePlayerRepo.getMyAvailability(req.user!.id);
    success(res, rows);
  } catch (e) { next(e); }
}

export async function listAvailable(req: Request, res: Response, next: NextFunction) {
  try {
    const sportType = req.query.sportType ? String(req.query.sportType) : undefined;
    if (!sportType) {
      throw new ValidationError('sportType query parameter is required');
    }
    const skillMin = req.query.skillMin ? Number(req.query.skillMin) : undefined;
    const skillMax = req.query.skillMax ? Number(req.query.skillMax) : undefined;

    const rows = await availablePlayerRepo.listBySport(
      sportType,
      skillMin,
      skillMax,
      req.user!.id // exclude self
    );
    success(res, rows);
  } catch (e) { next(e); }
}
