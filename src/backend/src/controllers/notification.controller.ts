import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as notificationRepo from '../repositories/notification.repository';

export async function getNotifications(req: Request, res: Response, next: NextFunction) {
  try {
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const offset = req.query.offset ? Number(req.query.offset) : 0;
    const notifications = await notificationRepo.findByUserId(req.user!.id, limit, offset);
    success(res, notifications);
  } catch (e) { next(e); }
}

export async function getUnreadCount(req: Request, res: Response, next: NextFunction) {
  try {
    const count = await notificationRepo.getUnreadCount(req.user!.id);
    success(res, { count });
  } catch (e) { next(e); }
}

export async function markAsRead(req: Request, res: Response, next: NextFunction) {
  try {
    const { notificationIds } = req.body;
    await notificationRepo.markAsRead(notificationIds, req.user!.id);
    success(res, { message: 'Notifications marked as read' });
  } catch (e) { next(e); }
}

export async function registerDeviceToken(req: Request, res: Response, next: NextFunction) {
  try {
    const { fcmToken, deviceType } = req.body;
    const token = await notificationRepo.registerDeviceToken(req.user!.id, fcmToken, deviceType);
    success(res, token);
  } catch (e) { next(e); }
}

export async function removeDeviceToken(req: Request, res: Response, next: NextFunction) {
  try {
    const { fcmToken } = req.body;
    await notificationRepo.removeDeviceToken(fcmToken);
    success(res, { message: 'Device token removed' });
  } catch (e) { next(e); }
}
