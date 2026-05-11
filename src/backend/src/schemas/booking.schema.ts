import { z } from 'zod';

export const createBookingSchema = z.object({
  timeSlotId: z.number().int().positive(),
  notes: z.string().max(500).optional().nullable(),
});

export const updateBookingStatusSchema = z.object({
  status: z.enum(['pending', 'approved', 'confirmed', 'completed', 'cancelled']),
});
