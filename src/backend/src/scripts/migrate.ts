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

    // ALTER TYPE ... ADD VALUE cannot run inside a transaction block in PostgreSQL.
    // If the migration contains such statements, we must run them outside a transaction.
    const hasAddValue = /ALTER\s+TYPE\s+\w+\s+ADD\s+VALUE/i.test(upSql);
    const hasConcurrently = /CONCURRENTLY/i.test(upSql);
    const needsNoTransaction = hasAddValue || hasConcurrently;

    try {
      if (needsNoTransaction) {
        // Run outside a transaction — split by semicolons and execute each statement
        const safeSql = upSql.replace(/\bCONCURRENTLY\b/gi, '');
        const statements = safeSql
          .split(/;\s*\n/)
          .map(s => s.trim())
          .filter(s => s.length > 0 && !s.startsWith('--'));

        for (const stmt of statements) {
          await pool.query(stmt);
        }
        await pool.query('INSERT INTO _migrations (filename) VALUES ($1)', [file]);
      } else {
        await pool.query('BEGIN');
        await pool.query(upSql);
        await pool.query('INSERT INTO _migrations (filename) VALUES ($1)', [file]);
        await pool.query('COMMIT');
      }
      console.log(`  ✅ ${file} (applied)`);
      ranCount++;
    } catch (err) {
      if (!needsNoTransaction) {
        await pool.query('ROLLBACK');
      }
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
