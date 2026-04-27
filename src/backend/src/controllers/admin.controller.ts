// Admin endpoints powering the owner dashboard at /admin (frontend).
// All routes here are gated by `requireAdmin` middleware.
import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as adminRepo from '../repositories/admin.repository';
import * as notificationRepo from '../repositories/notification.repository';
import { NotFoundError, ValidationError } from '../utils/errors';

export async function getOverview(_req: Request, res: Response, next: NextFunction) {
  try {
    const overview = await adminRepo.getPlatformOverview();
    success(res, overview);
  } catch (e) { next(e); }
}

export async function getPendingPartners(_req: Request, res: Response, next: NextFunction) {
  try {
    const partners = await adminRepo.findPendingPartners();
    success(res, partners);
  } catch (e) { next(e); }
}

export async function getAllPartners(_req: Request, res: Response, next: NextFunction) {
  try {
    const partners = await adminRepo.findAllPartners();
    success(res, partners);
  } catch (e) { next(e); }
}

export async function getPartner(req: Request, res: Response, next: NextFunction) {
  try {
    const partner = await adminRepo.findPartnerById(Number(req.params.id));
    if (!partner) throw new NotFoundError('Partner');
    success(res, partner);
  } catch (e) { next(e); }
}

export async function approvePartner(req: Request, res: Response, next: NextFunction) {
  try {
    const adminUserId = req.user!.id;
    const partnerId = Number(req.params.id);
    const updated = await adminRepo.approvePartner(partnerId, adminUserId);
    if (!updated) throw new NotFoundError('Partner');

    // Tell them the good news. Use 'general' type + data.kind for routing on mobile.
    try {
      await notificationRepo.createNotification(
        partnerId,
        'general',
        'Your partner account is approved',
        'You can now publish venues, coaches and time slots. Welcome aboard.',
        { kind: 'partner_approved' }
      );
    } catch (e) {
      console.error('Failed to send partner_approved notification:', e);
    }

    success(res, updated);
  } catch (e) { next(e); }
}

export async function rejectPartner(req: Request, res: Response, next: NextFunction) {
  try {
    const adminUserId = req.user!.id;
    const partnerId = Number(req.params.id);
    const reason = String(req.body?.reason ?? '').trim();
    if (!reason) throw new ValidationError('reason is required');

    const updated = await adminRepo.rejectPartner(partnerId, adminUserId, reason);
    if (!updated) throw new NotFoundError('Partner');

    try {
      await notificationRepo.createNotification(
        partnerId,
        'general',
        'Partner application needs changes',
        reason,
        { kind: 'partner_rejected', reason }
      );
    } catch (e) {
      console.error('Failed to send partner_rejected notification:', e);
    }

    success(res, updated);
  } catch (e) { next(e); }
}
