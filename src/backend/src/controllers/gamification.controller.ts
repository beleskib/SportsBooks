import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import * as gamificationRepo from '../repositories/gamification.repository';

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
