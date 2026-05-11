import { z } from 'zod';

export const generateSlotsSchema = z.object({
  venueId: z.number().int().positive().optional(),
  coachId: z.number().int().positive().optional(),
  dateFrom: z.string().min(1),
  dateTo: z.string().min(1),
  startHour: z.number().int().min(0).max(23).optional(),
  endHour: z.number().int().min(1).max(24).optional(),
  daysOfWeek: z.array(z.number().int().min(0).max(6)).optional(),
}).refine(
  (data) => data.venueId != null || data.coachId != null,
  { message: 'Either venueId or coachId must be provided' },
);
