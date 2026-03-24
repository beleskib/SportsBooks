import { Request, Response, NextFunction } from 'express';
import * as dashboardRepo from '../repositories/dashboard.repository';

export const getPartnerStats = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.id;

    const [stats, monthlyRevenue] = await Promise.all([
      dashboardRepo.getPartnerStats(userId),
      dashboardRepo.getPartnerMonthlyRevenue(userId),
    ]);

    // Assemble bookingsByStatus from flat counts
    const bookingsByStatus = [
      { status: 'pending', count: stats.pendingBookings },
      { status: 'confirmed', count: stats.confirmedBookings },
      { status: 'completed', count: stats.completedBookings },
      { status: 'cancelled', count: stats.cancelledBookings },
    ].filter(item => item.count > 0);

    const result = {
      totalBookings: stats.totalBookings,
      confirmedBookings: stats.confirmedBookings,
      totalRevenue: stats.totalRevenue,
      avgRating: stats.avgRating,
      totalReviews: stats.totalReviews,
      upcomingBookings: stats.upcomingBookings,
      revenueByMonth: monthlyRevenue,
      bookingsByStatus,
    };

    res.json({ data: result });
  } catch (error) {
    next(error);
  }
};
