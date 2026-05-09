import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ForbiddenError, ValidationError } from '../utils/errors';
import * as dmRepo from '../repositories/directMessage.repository';
import * as friendshipRepo from '../repositories/friendship.repository';
import * as userRepo from '../repositories/user.repository';
import * as notificationService from '../services/notification.service';

// ── Helpers ────────────────────────────────────────────────────────

async function requireFriendship(userId: number, friendUserId: number): Promise<void> {
  const friendship = await friendshipRepo.findFriendship(userId, friendUserId);
  if (!friendship || friendship.status !== 'accepted') {
    throw new ForbiddenError('You can only message friends');
  }
}

// ── Controller functions ───────────────────────────────────────────

/**
 * GET /api/dm
 * List all conversations ("inbox") with latest message + unread count.
 */
export async function getConversations(req: Request, res: Response, next: NextFunction) {
  try {
    const conversations = await dmRepo.getConversationList(req.user!.id);
    success(res, conversations);
  } catch (e) { next(e); }
}

/**
 * GET /api/dm/unread-count
 * Total unread DM count across all conversations.
 */
export async function getUnreadCount(req: Request, res: Response, next: NextFunction) {
  try {
    const unreadCount = await dmRepo.getUnreadCount(req.user!.id);
    success(res, { unreadCount });
  } catch (e) { next(e); }
}

/**
 * GET /api/dm/:friendUserId/messages?limit=50&offset=0
 * Get messages in a conversation. Auto-marks received messages as read.
 */
export async function getConversation(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const friendUserId = Number(req.params.friendUserId);

    await requireFriendship(userId, friendUserId);

    const limit = req.query.limit ? Number(req.query.limit) : 50;
    const offset = req.query.offset ? Number(req.query.offset) : 0;
    const messages = await dmRepo.getConversation(userId, friendUserId, limit, offset);

    // Auto-mark messages from the friend as read (fire-and-forget)
    dmRepo.markRead(userId, friendUserId).catch(() => {});

    success(res, messages);
  } catch (e) { next(e); }
}

/**
 * POST /api/dm/:friendUserId/messages
 * Send a direct message to a friend.
 * Body: { message: string }
 */
export async function sendMessage(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const friendUserId = Number(req.params.friendUserId);
    const { message } = req.body;

    if (!message || typeof message !== 'string' || message.trim().length === 0) {
      throw new ValidationError('Message text is required');
    }

    await requireFriendship(userId, friendUserId);

    const newMessage = await dmRepo.sendMessage(userId, friendUserId, message.trim());

    // Send push notification (fire-and-forget)
    const sender = await userRepo.findById(userId);
    const senderName = sender?.displayName || req.user!.email || 'Someone';
    const preview = message.trim().length > 100
      ? message.trim().substring(0, 97) + '...'
      : message.trim();
    notificationService
      .sendNotification(
        friendUserId,
        'direct_message',
        `${senderName} sent you a message`,
        preview,
        { senderUserId: String(userId) }
      )
      .catch(() => {});

    created(res, newMessage, 'Message sent');
  } catch (e) { next(e); }
}

/**
 * PUT /api/dm/:friendUserId/read
 * Mark all messages from a friend as read.
 */
export async function markRead(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const friendUserId = Number(req.params.friendUserId);

    const count = await dmRepo.markRead(userId, friendUserId);
    success(res, { markedRead: count });
  } catch (e) { next(e); }
}
