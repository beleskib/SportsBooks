import { z } from 'zod';

const SPORT_TYPES = [
  'basketball', 'football', 'tennis', 'paddle', 'volleyball', 'swimming',
  'boxing', 'mma', 'yoga', 'pilates', 'crossfit', 'running', 'cycling',
  'golf', 'badminton', 'table_tennis', 'handball', 'baseball', 'cricket',
] as const;

export const createMatchSchema = z.object({
  sportType: z.enum(SPORT_TYPES),
  matchType: z.string().min(1).max(50),
  title: z.string().min(1).max(200),
  matchDate: z.string().min(1),
  startTime: z.string().min(1),
  endTime: z.string().min(1),
  minPlayers: z.number().int().min(1).max(100),
  maxPlayers: z.number().int().min(1).max(100),
  bookingId: z.number().int().positive().optional().nullable(),
  venueId: z.number().int().positive().optional().nullable(),
  visibility: z.enum(['public', 'private', 'friends_only']).optional(),
  description: z.string().max(2000).optional().nullable(),
  minSkillLevel: z.number().int().min(1).max(10).optional().nullable(),
  maxSkillLevel: z.number().int().min(1).max(10).optional().nullable(),
  locationName: z.string().max(200).optional().nullable(),
  address: z.string().max(500).optional().nullable(),
  latitude: z.number().min(-90).max(90).optional().nullable(),
  longitude: z.number().min(-180).max(180).optional().nullable(),
  isFree: z.boolean().optional(),
  costPerPlayer: z.number().min(0).optional().nullable(),
  timeSlotId: z.number().int().positive().optional().nullable(),
  paymentType: z.enum(['host_pays', 'split', 'split_to_teams', 'cash_at_venue']).optional(),
});

export const updateMatchSchema = createMatchSchema.partial();

export const respondToJoinSchema = z.object({
  status: z.enum(['approved', 'declined']),
});

export const invitePlayerSchema = z.object({
  userId: z.number().int().positive(),
});

export const sendChatMessageSchema = z.object({
  message: z.string().min(1).max(2000).optional(),
  content: z.string().min(1).max(2000).optional(),
}).refine(
  (data) => data.message != null || data.content != null,
  { message: 'Either message or content must be provided' },
);

export const ratePlayerSchema = z.object({
  ratedId: z.number().int().positive(),
  skillRating: z.number().int().min(1).max(5),
  sportsmanshipRating: z.number().int().min(1).max(5),
  punctualityRating: z.number().int().min(1).max(5),
  comment: z.string().max(500).optional().nullable(),
});

export const createRecurrenceRuleSchema = z.object({
  frequency: z.enum(['daily', 'weekly', 'biweekly', 'monthly']),
  dayOfWeek: z.number().int().min(0).max(6),
  startTime: z.string().min(1),
  endTime: z.string().min(1),
  sportType: z.enum(SPORT_TYPES),
  title: z.string().min(1).max(200),
  minPlayers: z.number().int().min(1).max(100),
  maxPlayers: z.number().int().min(1).max(100),
  venueId: z.number().int().positive().optional().nullable(),
  locationName: z.string().max(200).optional().nullable(),
  address: z.string().max(500).optional().nullable(),
  latitude: z.number().min(-90).max(90).optional().nullable(),
  longitude: z.number().min(-180).max(180).optional().nullable(),
  minSkillLevel: z.number().int().min(1).max(10).optional().nullable(),
  maxSkillLevel: z.number().int().min(1).max(10).optional().nullable(),
});

export const updateRecurrenceRuleSchema = createRecurrenceRuleSchema.partial().extend({
  isActive: z.boolean().optional(),
  nextOccurrenceDate: z.string().optional().nullable(),
});
