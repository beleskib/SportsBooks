// ============================================================
// Global Search Types
// ============================================================

import { Venue } from './venue';
import { Coach } from './coach';
import { Match } from './match';

export interface GlobalSearchResults {
  venues: Venue[];
  coaches: Coach[];
  matches: Match[];
}
