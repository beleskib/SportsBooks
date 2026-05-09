import 'dotenv/config';
import express from 'express';
import helmet from 'helmet';
import cors from 'cors';
import pinoHttp from 'pino-http';
import { env } from './config/env';
import { logger } from './config/logger';
import { pool } from './config/database';
import { errorHandler } from './middleware/errorHandler';
import { authLimiter, apiLimiter } from './middleware/rateLimiter';
import routes from './routes';
import webhookRoutes from './routes/webhook.routes';
import { expirePendingBookings } from './services/bookingExpiry.service';
import { startReminderJob } from './services/bookingApprovalReminder.service';

const app = express();

// Middleware
app.use(helmet());
app.use(pinoHttp({ logger }));
app.use(cors({
  origin: process.env.NODE_ENV === 'production'
    ? process.env.CORS_ALLOWED_ORIGINS?.split(',') || []
    : true,
  credentials: true,
}));

// Stripe webhook needs raw body — must be registered BEFORE express.json()
app.use('/api/webhooks', webhookRoutes);

app.use(express.json());

// Health check
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Rate limiting — strict on auth, general cap on everything else under /api
app.use('/api/auth', authLimiter);
app.use('/api', apiLimiter);

// API routes
app.use('/api', routes);

// Error handler (must be last)
app.use(errorHandler);

const server = app.listen(env.port, () => {
  logger.info(`SportsBooks API running on port ${env.port}`);
  logger.info(`Health check: http://localhost:${env.port}/health`);
  logger.info(`API base:     http://localhost:${env.port}/api`);
});

const shutdown = async (signal: string) => {
  logger.info(`${signal} received. Shutting down gracefully...`);
  server.close(() => {
    logger.info('HTTP server closed');
    pool.end().then(() => {
      logger.info('Database pool drained');
      process.exit(0);
    });
  });
  // Force exit after 10 seconds
  setTimeout(() => {
    logger.error('Forced shutdown after timeout');
    process.exit(1);
  }, 10000);
};

process.on('SIGTERM', () => shutdown('SIGTERM'));
process.on('SIGINT', () => shutdown('SIGINT'));
process.on('uncaughtException', (err) => {
  logger.error(err, 'Uncaught exception');
  shutdown('uncaughtException');
});
process.on('unhandledRejection', (reason) => {
  logger.error({ reason }, 'Unhandled rejection');
  shutdown('unhandledRejection');
});

// Run booking expiry check every 15 minutes
setInterval(expirePendingBookings, 15 * 60 * 1000);
// Also run once on startup
expirePendingBookings();

// Partner approval reminder cron (+2h / +6h emails)
startReminderJob();

export default app;
