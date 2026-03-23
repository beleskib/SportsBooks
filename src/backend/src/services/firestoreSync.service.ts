import { getFirestoreDb } from '../config/firebase';
import type { VenueRow } from '../repositories/venue.repository';
import type { CoachRow } from '../repositories/coach.repository';

const VENUES_COLLECTION = 'venues';
const COACHES_COLLECTION = 'coaches';

export async function syncVenueToFirestore(venue: VenueRow): Promise<void> {
  try {
    const db = getFirestoreDb();
    if (!db) {
      console.warn('[FirestoreSync] Firestore not initialized, skipping venue sync');
      return;
    }

    await db.collection(VENUES_COLLECTION).doc(String(venue.id)).set({
      id: venue.id,
      ownerId: venue.ownerId,
      name: venue.name,
      description: venue.description,
      sportType: venue.sportType,
      pricePerHour: venue.pricePerHour,
      address: venue.address,
      city: venue.city,
      country: venue.country,
      latitude: venue.latitude,
      longitude: venue.longitude,
      phoneNumber: venue.phoneNumber,
      email: venue.email,
      avgRating: venue.avgRating,
      totalReviews: venue.totalReviews,
      isActive: venue.isActive,
      images: venue.images.map((img) => ({
        id: img.id,
        imageUrl: img.imageUrl,
        isPrimary: img.isPrimary,
        displayOrder: img.displayOrder,
      })),
      equipment: venue.equipment.map((eq) => ({
        id: eq.id,
        name: eq.name,
        description: eq.description,
        isIncluded: eq.isIncluded,
      })),
      activeDiscount: venue.activeDiscount
        ? {
            id: venue.activeDiscount.id,
            title: venue.activeDiscount.title,
            description: venue.activeDiscount.description,
            discountPercent: venue.activeDiscount.discountPercent,
            discountAmount: venue.activeDiscount.discountAmount,
            validFrom: venue.activeDiscount.validFrom,
            validUntil: venue.activeDiscount.validUntil,
          }
        : null,
      createdAt: venue.createdAt,
      updatedAt: venue.updatedAt,
    }, { merge: true });

    console.log(`[FirestoreSync] Venue ${venue.id} synced to Firestore`);
  } catch (error) {
    console.warn(`[FirestoreSync] Failed to sync venue ${venue.id}:`, error);
  }
}

export async function syncCoachToFirestore(coach: CoachRow): Promise<void> {
  try {
    const db = getFirestoreDb();
    if (!db) {
      console.warn('[FirestoreSync] Firestore not initialized, skipping coach sync');
      return;
    }

    await db.collection(COACHES_COLLECTION).doc(String(coach.id)).set({
      id: coach.id,
      userId: coach.userId,
      name: coach.name,
      bio: coach.bio,
      sportType: coach.sportType,
      specialization: coach.specialization,
      experienceYears: coach.experienceYears,
      pricePerHour: coach.pricePerHour,
      address: coach.address,
      city: coach.city,
      country: coach.country,
      latitude: coach.latitude,
      longitude: coach.longitude,
      phoneNumber: coach.phoneNumber,
      email: coach.email,
      avgRating: coach.avgRating,
      totalReviews: coach.totalReviews,
      isActive: coach.isActive,
      images: coach.images.map((img) => ({
        id: img.id,
        imageUrl: img.imageUrl,
        isPrimary: img.isPrimary,
        displayOrder: img.displayOrder,
      })),
      certifications: coach.certifications.map((cert) => ({
        id: cert.id,
        name: cert.name,
        issuingBody: cert.issuingBody,
        yearObtained: cert.yearObtained,
        certificateUrl: cert.certificateUrl,
      })),
      activeDiscount: coach.activeDiscount
        ? {
            id: coach.activeDiscount.id,
            title: coach.activeDiscount.title,
            description: coach.activeDiscount.description,
            discountPercent: coach.activeDiscount.discountPercent,
            discountAmount: coach.activeDiscount.discountAmount,
            validFrom: coach.activeDiscount.validFrom,
            validUntil: coach.activeDiscount.validUntil,
          }
        : null,
      createdAt: coach.createdAt,
      updatedAt: coach.updatedAt,
    }, { merge: true });

    console.log(`[FirestoreSync] Coach ${coach.id} synced to Firestore`);
  } catch (error) {
    console.warn(`[FirestoreSync] Failed to sync coach ${coach.id}:`, error);
  }
}

export async function softDeleteVenueInFirestore(id: number): Promise<void> {
  try {
    const db = getFirestoreDb();
    if (!db) return;

    await db.collection(VENUES_COLLECTION).doc(String(id)).update({
      isActive: false,
      updatedAt: new Date().toISOString(),
    });

    console.log(`[FirestoreSync] Venue ${id} soft-deleted in Firestore`);
  } catch (error) {
    console.warn(`[FirestoreSync] Failed to soft-delete venue ${id}:`, error);
  }
}

export async function softDeleteCoachInFirestore(id: number): Promise<void> {
  try {
    const db = getFirestoreDb();
    if (!db) return;

    await db.collection(COACHES_COLLECTION).doc(String(id)).update({
      isActive: false,
      updatedAt: new Date().toISOString(),
    });

    console.log(`[FirestoreSync] Coach ${id} soft-deleted in Firestore`);
  } catch (error) {
    console.warn(`[FirestoreSync] Failed to soft-delete coach ${id}:`, error);
  }
}
