import { z } from 'zod';

export const createReviewSchema = z.object({
  venueId: z.number().int().positive().optional().nullable(),
  coachId: z.number().int().positive().optional().nullable(),
  bookingId: z.number().int().positive().optional().nullable(),
  rating: z.number().int().min(1).max(5),
  comment: z.string().max(1000).optional().nullable(),
}).refine(
  (data) => data.venueId != null || data.coachId != null,
  { message: 'Either venueId or coachId must be provided' },
);
