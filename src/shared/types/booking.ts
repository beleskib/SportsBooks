import { BookingStatus } from '../enums';
import { BookingParticipant } from './bookingParticipant';
import { Coach } from './coach';
import { SplitPaymentSummary } from './splitPayment';
import { TimeSlot } from './timeSlot';
import { Venue } from './venue';

// ============================================================
// Booking types
// ============================================================

export interface Booking {
  id: number;
  playerId: number;
  timeSlotId: number | null;
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
  // v2-practical-ux: participants tagged at checkout and their attendance
  participants?: BookingParticipant[];
  splitPayment?: SplitPaymentSummary;
  expiresAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  timeSlotId: number;
  notes?: string;
  // v2-practical-ux: tag friends at booking time
  inviteUserIds?: number[];
  // v2-practical-ux: split the cost at checkout
  splitWith?: number[]; // user ids that should each pay an equal share
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
