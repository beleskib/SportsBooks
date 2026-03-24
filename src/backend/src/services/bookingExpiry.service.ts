import { query } from '../config/database';
import * as notificationRepo from '../repositories/notification.repository';

export async function expirePendingBookings(): Promise<void> {
  try {
    const result = await query('SELECT * FROM expire_pending_bookings()');

    for (const row of result.rows) {
      try {
        const entityName = row.expired_venue_name || row.expired_coach_name || 'your venue/coach';
        await notificationRepo.createNotification(
          row.expired_player_id,
          'booking_declined',
          'Booking Expired',
          `Your booking request for "${entityName}" was not responded to within 24 hours and has been automatically cancelled.`,
          { bookingId: String(row.expired_booking_id) }
        );
      } catch (e) {
        console.error(`Failed to send expiry notification for booking ${row.expired_booking_id}:`, e);
      }
    }

    if (result.rows.length > 0) {
      console.log(`Expired ${result.rows.length} pending booking(s)`);
    }
  } catch (error) {
    console.error('Error running booking expiry job:', error);
  }
}
