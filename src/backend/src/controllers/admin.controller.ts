// Admin endpoints powering the owner dashboard at /admin (frontend).
// All routes here are gated by `requireAdmin` middleware.
//
// The approval gate operates on individual listings (venues + coaches), not
// on partner accounts — see migration 0054. Admin reviews each listing,
// approves or rejects it; on approval the listing becomes visible to the public.
import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as adminRepo from '../repositories/admin.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import * as notificationRepo from '../repositories/notification.repository';
import {
  notifyPartnerListingApproved,
  notifyPartnerListingRejected,
} from '../services/listingApproval.service';
import { NotFoundError, ValidationError } from '../utils/errors';

export async function getOverview(_req: Request, res: Response, next: NextFunction) {
  try {
    const overview = await adminRepo.getPlatformOverview();
    success(res, overview);
  } catch (e) { next(e); }
}

/** Pending venue + coach queue for admin review. */
export async function getPendingListings(_req: Request, res: Response, next: NextFunction) {
  try {
    const listings = await adminRepo.findPendingListings();
    success(res, listings);
  } catch (e) { next(e); }
}

/** Full review detail for a single listing — used by the listing review page. */
export async function getListingDetail(req: Request, res: Response, next: NextFunction) {
  try {
    const type = req.params.type;
    const id = Number(req.params.id);
    if (type !== 'venue' && type !== 'coach') {
      throw new ValidationError('type must be venue or coach');
    }
    const detail = type === 'venue'
      ? await adminRepo.findVenueDetail(id)
      : await adminRepo.findCoachDetail(id);
    if (!detail) throw new NotFoundError('Listing');
    success(res, detail);
  } catch (e) { next(e); }
}

export async function approveListing(req: Request, res: Response, next: NextFunction) {
  try {
    const type = req.params.type;
    const id = Number(req.params.id);
    if (type !== 'venue' && type !== 'coach') {
      throw new ValidationError('type must be venue or coach');
    }
    const adminUserId = req.user!.id;

    const updated = type === 'venue'
      ? await venueRepo.approve(id, adminUserId)
      : await coachRepo.approve(id, adminUserId);
    if (!updated) throw new NotFoundError('Listing');

    const ownerId = type === 'venue'
      ? (updated as venueRepo.VenueRow).ownerId
      : (updated as coachRepo.CoachRow).userId;

    // Email + in-app notification — both fire-and-forget.
    notifyPartnerListingApproved(type, id);
    notificationRepo.createNotification(
      ownerId,
      'general',
      type === 'venue' ? 'Your venue is live' : 'Your coach profile is live',
      `"${updated.name}" passed review and is now visible to players.`,
      { kind: 'listing_approved', listingType: type, listingId: id }
    ).catch((e) => console.error('listing_approved notification:', e));

    success(res, updated);
  } catch (e) { next(e); }
}

export async function rejectListing(req: Request, res: Response, next: NextFunction) {
  try {
    const type = req.params.type;
    const id = Number(req.params.id);
    if (type !== 'venue' && type !== 'coach') {
      throw new ValidationError('type must be venue or coach');
    }
    const reason = String(req.body?.reason ?? '').trim();
    if (!reason) throw new ValidationError('reason is required');
    const adminUserId = req.user!.id;

    const updated = type === 'venue'
      ? await venueRepo.reject(id, adminUserId, reason)
      : await coachRepo.reject(id, adminUserId, reason);
    if (!updated) throw new NotFoundError('Listing');

    const ownerId = type === 'venue'
      ? (updated as venueRepo.VenueRow).ownerId
      : (updated as coachRepo.CoachRow).userId;

    notifyPartnerListingRejected(type, id, reason);
    notificationRepo.createNotification(
      ownerId,
      'general',
      type === 'venue' ? 'Venue needs changes' : 'Coach profile needs changes',
      reason,
      { kind: 'listing_rejected', listingType: type, listingId: id, reason }
    ).catch((e) => console.error('listing_rejected notification:', e));

    success(res, updated);
  } catch (e) { next(e); }
}
