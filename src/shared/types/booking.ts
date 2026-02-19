import { BookingStatus } from '../enums';
import { Coach } from './coach';
import { TimeSlot } from './timeSlot';
import { Venue } from './venue';

// ============================================================
// Booking types
// ============================================================

export interface Booking {
  id: number;
  playerId: number;
  timeSlotId: number;
  venueId: number | null;
  coachId: number | null;
  status: BookingStatus;
  totalPrice: number;
  notes: string | null;
  timeSlot?: TimeSlot;
  venue?: Venue;
  coach?: Coach;
  playerName?: string;
  playerEmail?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  timeSlotId: number;
  notes?: string;
}

export interface UpdateBookingStatusRequest {
  status: BookingStatus;
}

export interface BookingFilters {
  status?: BookingStatus;
  venueId?: number;
  coachId?: number;
  dateFrom?: string;
  dateTo?: string;
}
