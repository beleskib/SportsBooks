import { z } from 'zod';

const SPORT_TYPES = [
  'basketball', 'football', 'tennis', 'paddle', 'volleyball', 'swimming',
  'boxing', 'mma', 'yoga', 'pilates', 'crossfit', 'running', 'cycling',
  'golf', 'badminton', 'table_tennis', 'handball', 'baseball', 'cricket',
] as const;

export const createVenueSchema = z.object({
  name: z.string().min(1).max(200),
  sportType: z.enum(SPORT_TYPES),
  pricePerHour: z.number().positive().max(100000),
  address: z.string().min(1).max(500),
  description: z.string().max(2000).optional().nullable(),
  city: z.string().max(100).optional().nullable(),
  country: z.string().max(100).optional().nullable(),
  latitude: z.number().min(-90).max(90).optional().nullable(),
  longitude: z.number().min(-180).max(180).optional().nullable(),
  phoneNumber: z.string().max(20).optional().nullable(),
  email: z.string().email().optional().nullable(),
});

export const updateVenueSchema = z.object({
  name: z.string().min(1).max(200).optional(),
  description: z.string().max(2000).optional().nullable(),
  pricePerHour: z.number().positive().max(100000).optional(),
  address: z.string().min(1).max(500).optional(),
  city: z.string().max(100).optional().nullable(),
  country: z.string().max(100).optional().nullable(),
  latitude: z.number().min(-90).max(90).optional().nullable(),
  longitude: z.number().min(-180).max(180).optional().nullable(),
  phoneNumber: z.string().max(20).optional().nullable(),
  email: z.string().email().optional().nullable(),
  isActive: z.boolean().optional(),
});
