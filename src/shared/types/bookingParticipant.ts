// ============================================================
// Booking Participant types — v2-practical-ux
// Multi-player bookings with attendance tracking
// ============================================================

export type BookingParticipantStatus =
  | 'invited'
  | 'accepted'
  | 'declined'
  | 'attended'
  | 'no_show';

export interface BookingParticipant {
  id: number;
  bookingId: number;
  userId: number;
  status: BookingParticipantStatus;
  splitPaymentId: number | null;
  respondedAt: string | null;
  attendedAt: string | null;
  createdAt: string;
  updatedAt: string;
  // Optional joined fields
  displayName?: string | null;
  photoUrl?: string | null;
}

export interface InviteBookingParticipantsRequest {
  userIds: number[];
  // Optional: also split the cost with them
  splitCost?: boolean;
}

export interface RespondToBookingInviteRequest {
  accept: boolean;
}

export interface MarkAttendanceRequest {
  // userId → attended? false marks no_show
  attendance: Array<{ userId: number; attended: boolean }>;
}
