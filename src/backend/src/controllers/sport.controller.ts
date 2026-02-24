import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as sportRepo from '../repositories/sport.repository';
import { NotFoundError } from '../utils/errors';

export async function getAll(_req: Request, res: Response, next: NextFunction) {
  try {
    const sports = await sportRepo.findAll();
    success(res, sports);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const sport = await sportRepo.findById(Number(req.params.id));
    if (!sport) throw new NotFoundError('Sport category');
    success(res, sport);
  } catch (e) { next(e); }
}
