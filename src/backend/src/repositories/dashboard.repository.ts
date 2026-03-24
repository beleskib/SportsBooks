import { query } from '../config/database';

export interface PartnerStatsRow {
  totalBookings: number;
  confirmedBookings: number;
  pendingBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  upcomingBookings: number;
  totalRevenue: number;
  avgRating: number;
  totalReviews: number;
}

export interface MonthlyRevenueRow {
  month: string;
  revenue: number;
  bookingCount: number;
}

export async function getPartnerStats(userId: number): Promise<PartnerStatsRow> {
  const result = await query('SELECT * FROM get_partner_dashboard_stats($1)', [userId]);
  const row = result.rows[0];
  if (!row) {
    return {
      totalBookings: 0, confirmedBookings: 0, pendingBookings: 0,
      completedBookings: 0, cancelledBookings: 0, upcomingBookings: 0,
      totalRevenue: 0, avgRating: 0, totalReviews: 0
    };
  }
  return {
    totalBookings: Number(row.total_bookings) || 0,
    confirmedBookings: Number(row.confirmed_bookings) || 0,
    pendingBookings: Number(row.pending_bookings) || 0,
    completedBookings: Number(row.completed_bookings) || 0,
    cancelledBookings: Number(row.cancelled_bookings) || 0,
    upcomingBookings: Number(row.upcoming_bookings) || 0,
    totalRevenue: Number(row.total_revenue) || 0,
    avgRating: Number(row.avg_rating) || 0,
    totalReviews: Number(row.total_reviews) || 0,
  };
}

export async function getPartnerMonthlyRevenue(userId: number, months: number = 6): Promise<MonthlyRevenueRow[]> {
  const result = await query(`
    SELECT
      to_char(ts.slot_date, 'YYYY-MM') AS month,
      COALESCE(SUM(b.total_price), 0) AS revenue,
      COUNT(b.id) AS booking_count
    FROM bookings b
    JOIN time_slots ts ON ts.id = b.time_slot_id
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    WHERE (v.owner_id = $1 OR c.user_id = $1)
      AND b.status IN ('confirmed', 'completed')
      AND ts.slot_date >= date_trunc('month', CURRENT_DATE) - ($2 || ' months')::interval
    GROUP BY to_char(ts.slot_date, 'YYYY-MM')
    ORDER BY month ASC
  `, [userId, months]);

  return result.rows.map(row => ({
    month: row.month,
    revenue: Number(row.revenue) || 0,
    bookingCount: Number(row.booking_count) || 0,
  }));
}
