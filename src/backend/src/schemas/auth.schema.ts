import { z } from 'zod';

export const registerSchema = z.object({
  firebaseUid: z.string().min(1).optional(),
  email: z.string().email().optional(),
  displayName: z.string().min(1).max(100),
  photoUrl: z.string().url().optional().nullable(),
});
