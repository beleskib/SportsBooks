import { Pool } from 'pg';
import { env } from './env';
import { logger } from './logger';

export const pool = new Pool({
  connectionString: env.databaseUrl,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
  ssl: env.databaseUrl.includes('supabase.com')
    ? { rejectUnauthorized: false }
    : false,
});

pool.on('error', (err) => {
  logger.error(err, 'Unexpected database pool error');
});

export const query = (text: string, params?: any[]) => pool.query(text, params);

/** Run a callback inside a single transaction. Auto-commits on success, rolls back on error. */
export async function withTransaction<T>(fn: (client: import('pg').PoolClient) => Promise<T>): Promise<T> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await fn(client);
    await client.query('COMMIT');
    return result;
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}
