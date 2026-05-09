import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as discountRepo from '../repositories/discount.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { ForbiddenError, NotFoundError, ValidationError } from '../utils/errors';

/**
 * Check whether the requesting user owns the venue or coach referenced by
 * venueId / coachId.  Returns true for admins unconditionally.
 */
async function verifyEntityOwnership(
  userId: number,
  userRole: string,
  venueId: number | null | undefined,
  coachId: number | null | undefined,
): Promise<boolean> {
  if (userRole === 'admin') return true;

  if (venueId) {
    const venue = await venueRepo.findById(venueId);
    if (venue && Number(venue.ownerId) === userId) return true;
  }
  if (coachId) {
    const coach = await coachRepo.findById(coachId);
    if (coach && Number(coach.userId) === userId) return true;
  }
  return false;
}

export async function getForVenue(req: Request, res: Response, next: NextFunction) {
  try {
    const discounts = await discountRepo.findByVenueId(Number(req.params.venueId));
    success(res, discounts);
  } catch (e) { next(e); }
}

export async function getForCoach(req: Request, res: Response, next: NextFunction) {
  try {
    const discounts = await discountRepo.findByCoachId(Number(req.params.coachId));
    success(res, discounts);
  } catch (e) { next(e); }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const userRole = req.user!.role;
    const { venueId, coachId } = req.body;

    if (!venueId && !coachId) {
      throw new ValidationError('Either venueId or coachId is required');
    }

    // Verify the requesting user owns the target venue or coach
    const isOwner = await verifyEntityOwnership(userId, userRole, venueId, coachId);
    if (!isOwner) {
      throw new ForbiddenError('Forbidden');
    }

    const discount = await discountRepo.create(req.body);
    created(res, discount, 'Discount created');
  } catch (e) { next(e); }
}

export async function update(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const userRole = req.user!.role;

    // Fetch the existing discount to check ownership
    const existing = await discountRepo.findById(Number(req.params.id));
    if (!existing) throw new NotFoundError('Discount');

    const isOwner = await verifyEntityOwnership(
      userId, userRole, existing.venueId, existing.coachId,
    );
    if (!isOwner) {
      throw new ForbiddenError('Forbidden');
    }

    const discount = await discountRepo.update(Number(req.params.id), req.body);
    success(res, discount);
  } catch (e) { next(e); }
}
