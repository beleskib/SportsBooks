import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import { ValidationError, NotFoundError } from '../utils/errors';
import * as gamificationRepo from '../repositories/gamification.repository';
import * as bookingRepo from '../repositories/booking.repository';

export async function getMyLevel(req: Request, res: Response, next: NextFunction) {
  try {
    const level = await gamificationRepo.getOrCreatePlayerLevel(req.user!.id);
    success(res, level);
  } catch (e) { next(e); }
}

export async function getMyXpHistory(req: Request, res: Response, next: NextFunction) {
  try {
    const limit = req.query.limit ? Number(req.query.limit) : 50;
    const history = await gamificationRepo.getXpHistory(req.user!.id, limit);
    success(res, history);
  } catch (e) { next(e); }
}

export async function getAchievements(_req: Request, res: Response, next: NextFunction) {
  try {
    const achievements = await gamificationRepo.getAllAchievements();
    success(res, achievements);
  } catch (e) { next(e); }
}

export async function getMyAchievements(req: Request, res: Response, next: NextFunction) {
  try {
    const achievements = await gamificationRepo.getPlayerAchievements(req.user!.id);
    success(res, achievements);
  } catch (e) { next(e); }
}

export async function getMyStats(req: Request, res: Response, next: NextFunction) {
  try {
    const stats = await gamificationRepo.getPlayerStats(req.user!.id);
    success(res, stats);
  } catch (e) { next(e); }
}

export async function checkAndAwardAchievements(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;

    // 1. Get player stats
    const stats = await gamificationRepo.getPlayerStats(userId);

    // 2. Get all active achievements
    const allAchievements = await gamificationRepo.getAllAchievements();

    // 3. Get player's already-earned achievements
    const earnedAchievements = await gamificationRepo.getPlayerAchievements(userId);
    const earnedIds = new Set(earnedAchievements.map((a) => a.achievementId));

    // 4. Check each unearned achievement against player stats
    const newlyEarned: gamificationRepo.PlayerAchievementRow[] = [];

    for (const achievement of allAchievements) {
      if (earnedIds.has(achievement.id)) continue;

      const met = checkCriteria(achievement.criteriaType, achievement.criteriaValue, stats);
      if (!met) continue;

      // 5. Award achievement and give XP reward
      const awarded = await gamificationRepo.awardAchievement(userId, achievement.id);
      newlyEarned.push(awarded);

      if (achievement.xpReward > 0) {
        await gamificationRepo.addXpTransaction(
          userId,
          achievement.xpReward,
          'achievement_earned',
          achievement.id,
          `Earned achievement: ${achievement.name}`
        );
      }
    }

    // 6. Update player level if any XP was awarded
    let level: gamificationRepo.PlayerLevelRow | null = null;
    if (newlyEarned.length > 0) {
      level = await gamificationRepo.updatePlayerLevel(userId);
    } else {
      level = await gamificationRepo.getOrCreatePlayerLevel(userId);
    }

    // 7. Return newly earned achievements
    success(res, {
      newlyEarned,
      totalNewAchievements: newlyEarned.length,
      level,
    });
  } catch (e) { next(e); }
}

// ── XP Redemption ───────────────────────────────────────────────────

export async function redeemXp(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;
    const { bookingId, xpAmount } = req.body;

    if (!bookingId || !xpAmount || xpAmount <= 0) {
      throw new ValidationError('bookingId and a positive xpAmount are required');
    }

    // 1. Validate booking exists, belongs to player, is in approved status
    const booking = await bookingRepo.findById(bookingId);
    if (!booking) throw new NotFoundError('Booking');
    if (booking.playerId !== userId) {
      throw new ValidationError('You can only redeem XP for your own bookings');
    }
    if (booking.status !== 'approved') {
      throw new ValidationError('Booking must be in approved status to redeem XP');
    }

    // 2. Validate player has enough XP
    const playerLevel = await gamificationRepo.getOrCreatePlayerLevel(userId);
    if (playerLevel.totalXp < xpAmount) {
      throw new ValidationError(
        `Insufficient XP. You have ${playerLevel.totalXp} XP but tried to redeem ${xpAmount}`
      );
    }

    // 3. Calculate discount: 100 XP = 1 MKD
    const rawDiscount = xpAmount / 100;

    // 4. Cap discount at booking total price (never go negative)
    const existingDiscount = await gamificationRepo.getXpRedemptionDiscount(bookingId);
    const maxAdditionalDiscount = booking.totalPrice - existingDiscount;
    const discountAmount = Math.min(rawDiscount, maxAdditionalDiscount);
    const actualXpSpent = Math.round(discountAmount * 100);

    if (actualXpSpent <= 0) {
      throw new ValidationError('This booking already has the maximum XP discount applied');
    }

    // 5. Create XP transaction with negative amount
    await gamificationRepo.addXpTransaction(
      userId,
      -actualXpSpent,
      'xp_redemption',
      bookingId,
      `Redeemed ${actualXpSpent} XP for ${discountAmount.toFixed(2)} MKD discount on booking #${bookingId}`
    );

    // 6. Update player level (recalculate from sum of all transactions)
    const updatedLevel = await gamificationRepo.updatePlayerLevel(userId);

    success(res, {
      xpSpent: actualXpSpent,
      discountAmount,
      remainingXp: updatedLevel.totalXp,
      newLevel: updatedLevel.currentLevel,
    });
  } catch (e) { next(e); }
}

// ── Criteria evaluation ─────────────────────────────────────────────

function checkCriteria(
  criteriaType: string,
  criteriaValue: number,
  stats: gamificationRepo.PlayerStats
): boolean {
  switch (criteriaType) {
    case 'booking_count':
      return stats.completedBookings >= criteriaValue;
    case 'match_count':
      return stats.completedMatches >= criteriaValue;
    case 'review_count':
      return stats.totalReviews >= criteriaValue;
    case 'friend_count':
      return stats.totalFriends >= criteriaValue;
    case 'sport_variety':
      return stats.uniqueSportsBooked >= criteriaValue;
    case 'early_bird':
      return stats.hasEarlyBirdBooking;
    case 'night_owl':
      return stats.hasNightOwlBooking;
    default:
      return false;
  }
}
