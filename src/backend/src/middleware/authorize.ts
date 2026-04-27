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

/**
 * Stricter than `requirePartnerOrAdmin`: a partner must have been approved
 * by an admin before they can list venues/coaches, take bookings, etc.
 *
 * Admins bypass the gate entirely (they're trusted by definition).
 *
 * Wire this on every "creates inventory the public can see/book" route:
 *   - venues: POST/PUT/DELETE
 *   - coaches: POST/PUT/DELETE
 *   - timeSlots: bulk-create / publish
 *
 * Read-only routes (GET /mine, GET /:id) deliberately skip this so a
 * pending partner can still log in, see "you are awaiting approval",
 * and prepare draft content.
 */
export function requireApprovedPartner(req: Request, _res: Response, next: NextFunction) {
  const role = req.user?.role;
  if (role === 'admin') return next();
  if (role !== 'partner') {
    return next(new ForbiddenError('Partner or admin access required'));
  }
  if (!req.user?.partnerApprovedAt) {
    return next(
      new ForbiddenError(
        'Your partner account is awaiting admin approval. ' +
        'You can sign in and prepare content, but listings stay private until approved.'
      )
    );
  }
  next();
}
