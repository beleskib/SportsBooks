import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { env } from './config/env';
import { errorHandler } from './middleware/errorHandler';
import { authLimiter, apiLimiter } from './middleware/rateLimiter';
import routes from './routes';
import webhookRoutes from './routes/webhook.routes';
import { expirePendingBookings } from './services/bookingExpiry.service';
import { startReminderJob } from './services/bookingApprovalReminder.service';

const app = express();

// Middleware
app.use(cors());

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

app.listen(env.port, () => {
  console.log(`🚀 SportsBooks API running on port ${env.port}`);
  console.log(`   Health check: http://localhost:${env.port}/health`);
  console.log(`   API base:     http://localhost:${env.port}/api`);
});

// Run booking expiry check every 15 minutes
setInterval(expirePendingBookings, 15 * 60 * 1000);
// Also run once on startup
expirePendingBookings();

// Partner approval reminder cron (+2h / +6h emails)
startReminderJob();

export default app;
