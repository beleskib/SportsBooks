import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ValidationError } from '../utils/errors';
import * as cardService from '../services/cardManagement.service';
import * as userRepo from '../repositories/user.repository';

// ============================================================
// Card Controller
// Endpoints for managing saved payment methods (cards).
// All endpoints require authentication.
// ============================================================

async function resolveUserId(req: Request): Promise<number> {
  if (req.user?.id) return req.user.id;
  const firebaseUid = (req as any).firebaseUid;
  if (firebaseUid) {
    const user = await userRepo.findByFirebaseUid(firebaseUid);
    if (user) return user.id;
  }
  throw new ValidationError('Unable to resolve user');
}

/**
 * POST /api/cards/setup-intent
 * Create a Stripe SetupIntent to save a new card.
 * Returns clientSecret + ephemeralKey for the mobile SDK.
 */
export async function createSetupIntent(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const result = await cardService.createSetupIntent(userId);
    created(res, result, 'SetupIntent created');
  } catch (e) {
    next(e);
  }
}

/**
 * GET /api/cards
 * List all saved payment methods (cards) for the authenticated user.
 */
export async function listCards(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const cards = await cardService.listSavedCards(userId);
    success(res, cards);
  } catch (e) {
    next(e);
  }
}

/**
 * DELETE /api/cards/:paymentMethodId
 * Remove a saved card.
 */
export async function deleteCard(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const paymentMethodId = String(req.params.paymentMethodId);

    if (!paymentMethodId) {
      throw new ValidationError('paymentMethodId is required');
    }

    await cardService.deleteSavedCard(userId, paymentMethodId);
    success(res, null, 'Card removed');
  } catch (e) {
    next(e);
  }
}

/**
 * PUT /api/cards/:paymentMethodId/default
 * Set a card as the default payment method.
 */
export async function setDefault(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = await resolveUserId(req);
    const paymentMethodId = String(req.params.paymentMethodId);

    if (!paymentMethodId) {
      throw new ValidationError('paymentMethodId is required');
    }

    await cardService.setDefaultCard(userId, paymentMethodId);
    success(res, null, 'Default card updated');
  } catch (e) {
    next(e);
  }
}
