// ============================================================
// Canonical skill-level labels for matchmaking filters.
// DB stores INTEGER 1..N (currently BETWEEN 1 AND 5 — legacy
// range preserved for backward compatibility), UI displays
// these 4 labels. Level 5 is collapsed into 'competitive' so
// existing lobbies/matches with max = 5 still render correctly.
//
// This is the single source of truth — Android, iOS, and web
// all import from here so labels never drift across platforms.
// ============================================================

export const PLAYER_SKILL_LEVELS = ['beginner', 'intermediate', 'advanced', 'competitive'] as const
export type PlayerSkillLevel = typeof PLAYER_SKILL_LEVELS[number]

export interface PlayerSkillLevelMeta {
  numeric: number // value stored in DB
  label: PlayerSkillLevel
  displayName: string
  description: string
}

/**
 * Ordered list of all skill levels with the numeric value we persist and
 * the human-readable strings we show. Iterate this for dropdowns / chips.
 */
export const SKILL_LEVEL_META: readonly PlayerSkillLevelMeta[] = [
  {
    numeric: 1,
    label: 'beginner',
    displayName: 'Beginner',
    description: 'First time or still learning the basics.',
  },
  {
    numeric: 2,
    label: 'intermediate',
    displayName: 'Intermediate',
    description: 'Comfortable with the rules, can rally / run plays.',
  },
  {
    numeric: 3,
    label: 'advanced',
    displayName: 'Advanced',
    description: 'Plays regularly, solid technique and tactics.',
  },
  {
    numeric: 4,
    label: 'competitive',
    displayName: 'Competitive',
    description: 'Tournament / league level play.',
  },
] as const

/** Numeric → label. Treats legacy DB value 5 as 'competitive' so historical data still shows correctly. */
export function skillLevelFromNumeric(n: number | null | undefined): PlayerSkillLevel | null {
  if (n == null) return null
  if (n <= 1) return 'beginner'
  if (n === 2) return 'intermediate'
  if (n === 3) return 'advanced'
  return 'competitive' // 4 or legacy 5
}

/** Label → numeric, for writing to DB. */
export function numericFromPlayerSkillLevel(s: PlayerSkillLevel): number {
  const found = SKILL_LEVEL_META.find(m => m.label === s)
  return found ? found.numeric : 1
}

/** Numeric → human string (e.g. for a badge). Falls back to `Lvl N` for unexpected values. */
export function skillLevelDisplayName(n: number | null | undefined): string {
  const label = skillLevelFromNumeric(n)
  if (!label) return '—'
  return SKILL_LEVEL_META.find(m => m.label === label)!.displayName
}

/** Range display: "Beginner – Advanced" or just "Beginner" if min===max. */
export function skillLevelRangeDisplay(
  min: number | null | undefined,
  max: number | null | undefined,
): string | null {
  const lo = skillLevelDisplayName(min)
  const hi = skillLevelDisplayName(max)
  if (lo === '—' && hi === '—') return null
  if (lo === hi) return lo
  if (lo === '—') return `Up to ${hi}`
  if (hi === '—') return `${lo}+`
  return `${lo} – ${hi}`
}
