import { getFirestoreDb } from '../config/firebase';
import * as bookingRepo from '../repositories/booking.repository';

const BOOKINGS_COLLECTION = 'bookings';

/**
 * Sync a booking to Firestore for real-time client updates.
 * Fetches the latest joined data (player, venue/coach, time slot) and
 * writes it to the `bookings` collection keyed by booking ID.
 *
 * Always called fire-and-forget: `syncBookingToFirestore(id).catch(() => {})`
 */
export async function syncBookingToFirestore(bookingId: number): Promise<void> {
  try {
    const booking = await bookingRepo.findById(bookingId);
    if (!booking) return;

    const db = getFirestoreDb();
    if (!db) return;

    await db.collection(BOOKINGS_COLLECTION).doc(String(bookingId)).set(
      {
        id: booking.id,
        playerId: booking.playerId,
        playerName: booking.playerName ?? null,
        playerEmail: booking.playerEmail ?? null,
        status: booking.status,
        totalPrice: booking.totalPrice,
        notes: booking.notes ?? null,
        providerType: booking.venueId ? 'venue' : 'coach',
        providerName: booking.venue?.name ?? booking.coach?.name ?? null,
        providerAddress: booking.venue?.address ?? null,
        sportType: booking.venue?.sportType ?? booking.coach?.sportType ?? null,
        venueId: booking.venueId ?? null,
        coachId: booking.coachId ?? null,
        slotDate: booking.timeSlot?.slotDate ?? null,
        startTime: booking.timeSlot?.startTime ?? null,
        endTime: booking.timeSlot?.endTime ?? null,
        createdAt: booking.createdAt,
        updatedAt: new Date().toISOString(),
      },
      { merge: true },
    );
  } catch (e) {
    console.error('Firestore booking sync failed:', e);
  }
}

/**
 * Bulk-sync multiple bookings (used by autoCompletePastBookings).
 * Each booking is synced independently — one failure doesn't block others.
 */
export async function syncBookingsToFirestore(bookingIds: number[]): Promise<void> {
  await Promise.allSettled(
    bookingIds.map((id) => syncBookingToFirestore(id)),
  );
}

/**
 * Delete a booking document from Firestore (e.g., if permanently removed).
 */
export async function deleteBookingFromFirestore(bookingId: number): Promise<void> {
  try {
    const db = getFirestoreDb();
    if (!db) return;
    await db.collection(BOOKINGS_COLLECTION).doc(String(bookingId)).delete();
  } catch (e) {
    console.error('Firestore booking delete failed:', e);
  }
}
