import { Router, Request, Response } from 'express';
import { pool } from '../config/database';

const router = Router();

router.get('/', async (_req: Request, res: Response) => {
  let dbStatus = 'ok';
  try {
    await pool.query('SELECT 1');
  } catch {
    dbStatus = 'error';
  }
  res.json({ status: 'ok', db: dbStatus, timestamp: new Date().toISOString() });
});

export default router;
