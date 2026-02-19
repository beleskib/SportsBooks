import { DayOfWeek } from '../enums';

// ============================================================
// Time Slot types
// ============================================================

export interface TimeSlot {
  id: number;
  venueId: number | null;
  coachId: number | null;
  slotDate: string;       // "2024-01-15"
  startTime: string;      // "09:00"
  endTime: string;        // "10:00"
  isAvailable: boolean;
  priceOverride: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface TimeSlotFilters {
  venueId?: number;
  coachId?: number;
  dateFrom: string;
  dateTo: string;
  availableOnly?: boolean;
}

export interface GenerateSlotsRequest {
  venueId?: number;
  coachId?: number;
  dateFrom: string;
  dateTo: string;
}

export interface AvailabilityTemplate {
  id: number;
  venueId: number | null;
  coachId: number | null;
  dayOfWeek: DayOfWeek;
  startTime: string;     // "09:00"
  endTime: string;       // "22:00"
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAvailabilityTemplateRequest {
  venueId?: number;
  coachId?: number;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
}
