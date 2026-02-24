import 'dotenv/config';
import fs from 'fs';
import path from 'path';
import { pool } from '../config/database';

const MIGRATIONS_DIR = path.resolve(__dirname, '../../../../database/migrations');

async function migrate() {
  console.log('🔄 Running database migrations...\n');

  // Ensure migrations tracking table exists
  await pool.query(`
    CREATE TABLE IF NOT EXISTS _migrations (
      id SERIAL PRIMARY KEY,
      filename TEXT NOT NULL UNIQUE,
      applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  // Get already-applied migrations
  const { rows: applied } = await pool.query(
    'SELECT filename FROM _migrations ORDER BY filename'
  );
  const appliedSet = new Set(applied.map((r: { filename: string }) => r.filename));

  // Read migration files
  const files = fs.readdirSync(MIGRATIONS_DIR)
    .filter((f: string) => f.endsWith('.sql'))
    .sort();

  if (files.length === 0) {
    console.log('No migration files found.');
    await pool.end();
    return;
  }

  let ranCount = 0;

  for (const file of files) {
    if (appliedSet.has(file)) {
      console.log(`  ✅ ${file} (already applied)`);
      continue;
    }

    const filePath = path.join(MIGRATIONS_DIR, file);
    const sql = fs.readFileSync(filePath, 'utf-8');

    // Extract only the UP section if markers exist
    let upSql = sql;
    const upMatch = sql.indexOf('-- UP');
    const downMatch = sql.indexOf('-- DOWN');
    if (upMatch !== -1 && downMatch !== -1) {
      upSql = sql.substring(upMatch + '-- UP'.length, downMatch).trim();
    } else if (upMatch !== -1) {
      upSql = sql.substring(upMatch + '-- UP'.length).trim();
    }

    // Check if migration contains CONCURRENTLY (can't run in transaction)
    const hasConcurrently = /CONCURRENTLY/i.test(upSql);

    try {
      if (hasConcurrently) {
        // Split statements and run each outside a transaction
        // Replace CONCURRENTLY with regular index creation to allow transactional safety
        const safeSql = upSql.replace(/\bCONCURRENTLY\b/gi, '');
        await pool.query('BEGIN');
        await pool.query(safeSql);
        await pool.query('INSERT INTO _migrations (filename) VALUES ($1)', [file]);
        await pool.query('COMMIT');
      } else {
        await pool.query('BEGIN');
        await pool.query(upSql);
        await pool.query('INSERT INTO _migrations (filename) VALUES ($1)', [file]);
        await pool.query('COMMIT');
      }
      console.log(`  ✅ ${file} (applied)`);
      ranCount++;
    } catch (err) {
      await pool.query('ROLLBACK');
      console.error(`  ❌ ${file} FAILED:`);
      console.error(err);
      process.exit(1);
    }
  }

  console.log(`\n✅ Migrations complete. ${ranCount} new, ${files.length - ranCount} already applied.`);
  await pool.end();
}

migrate().catch((err) => {
  console.error('Migration runner error:', err);
  process.exit(1);
});
