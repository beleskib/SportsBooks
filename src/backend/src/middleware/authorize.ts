import { Request, Response, NextFunction } from 'express';
import { ForbiddenError } from '../utils/errors';

export function requireAdmin(req: Request, _res: Response, next: NextFunction) {
  if (req.user?.role !== 'admin') {
    return next(new ForbiddenError('Admin access required'));
  }
  next();
}

export function requirePartnerOrAdmin(req: Request, _res: Response, next: NextFunction) {
  if (req.user?.role !== 'partner' && req.user?.role !== 'admin') {
    return next(new ForbiddenError('Partner or admin access required'));
  }
  next();
}
