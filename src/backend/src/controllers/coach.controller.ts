import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as coachRepo from '../repositories/coach.repository';
import { NotFoundError } from '../utils/errors';

export async function getBySport(req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.findBySport(req.params.sportType);
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const coach = await coachRepo.findById(Number(req.params.id));
    if (!coach) throw new NotFoundError('Coach');
    success(res, coach);
  } catch (e) { next(e); }
}

export async function getTopDeals(_req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.findTopDeals();
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function search(req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.search(String(req.query.q || ''));
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function getMine(req: Request, res: Response, next: NextFunction) {
  try {
    const coach = await coachRepo.findByUserId(req.user!.id);
    success(res, coach);
  } catch (e) { next(e); }
}
