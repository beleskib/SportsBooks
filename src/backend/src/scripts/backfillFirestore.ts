import 'dotenv/config';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { syncVenueToFirestore, syncCoachToFirestore } from '../services/firestoreSync.service';
import { pool } from '../config/database';

async function backfill() {
  console.log('🔄 Backfilling Firestore collections from PostgreSQL...\n');

  // --- Venues ---
  console.log('Syncing venues...');
  const venueResult = await venueRepo.findAll({ page: 1, limit: 100 });
  const venues = venueResult.data;
  console.log(`   Found ${venues.length} venue(s) in PostgreSQL`);

  let venueSuccess = 0;
  for (const venue of venues) {
    try {
      await syncVenueToFirestore(venue);
      venueSuccess++;
    } catch (error) {
      console.error(`   Failed to sync venue ${venue.id} (${venue.name}):`, error);
    }
  }
  console.log(`   ${venueSuccess}/${venues.length} venues synced to Firestore\n`);

  // --- Coaches ---
  console.log('Syncing coaches...');
  const coachResult = await coachRepo.findAll({ page: 1, limit: 100 });
  const coaches = coachResult.data;
  console.log(`   Found ${coaches.length} coach(es) in PostgreSQL`);

  let coachSuccess = 0;
  for (const coach of coaches) {
    try {
      await syncCoachToFirestore(coach);
      coachSuccess++;
    } catch (error) {
      console.error(`   Failed to sync coach ${coach.id} (${coach.name}):`, error);
    }
  }
  console.log(`   ${coachSuccess}/${coaches.length} coaches synced to Firestore\n`);

  // --- Summary ---
  console.log('Backfill complete!');
  console.log(`   Venues:  ${venueSuccess}/${venues.length}`);
  console.log(`   Coaches: ${coachSuccess}/${coaches.length}`);

  await pool.end();
}

backfill().catch((err) => {
  console.error('Backfill failed:', err);
  process.exit(1);
});
