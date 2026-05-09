import { Pool } from 'pg';
import { env } from './env';
import { logger } from './logger';

export const pool = new Pool({
  connectionString: env.databaseUrl,
});

pool.on('error', (err) => {
  logger.error(err, 'Unexpected database pool error');
});

export const query = (text: string, params?: any[]) => pool.query(text, params);
