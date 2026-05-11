import { z } from 'zod';

export const createPaymentIntentSchema = z.object({
  bookingId: z.number().int().positive(),
});
