import { BookingStatus } from '../enums';

// ============================================================
// Dashboard types (Partner + Admin)
// ============================================================

export interface PartnerDashboardStats {
  totalBookings: number;
  confirmedBookings: number;
  totalRevenue: number;
  avgRating: number;
  totalReviews: number;
  upcomingBookings: number;
  revenueByMonth: MonthlyRevenue[];
  bookingsByStatus: BookingStatusCount[];
}

export interface MonthlyRevenue {
  month: string;       // "2024-01"
  revenue: number;
  bookingCount: number;
}

export interface BookingStatusCount {
  status: BookingStatus;
  count: number;
}

export interface AdminDashboardStats {
  totalUsers: number;
  totalPlayers: number;
  totalPartners: number;
  totalVenues: number;
  totalCoaches: number;
  totalBookings: number;
  totalRevenue: number;
  revenueByMonth: MonthlyRevenue[];
  bookingsByStatus: BookingStatusCount[];
  popularSports: SportPopularity[];
}

export interface SportPopularity {
  sportType: string;
  venueCount: number;
  coachCount: number;
  bookingCount: number;
}
