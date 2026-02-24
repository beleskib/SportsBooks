import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { env } from './config/env';
import { errorHandler } from './middleware/errorHandler';
import routes from './routes';

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Health check
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API routes
app.use('/api', routes);

// Error handler (must be last)
app.use(errorHandler);

app.listen(env.port, () => {
  console.log(`🚀 SportsBooks API running on port ${env.port}`);
  console.log(`   Health check: http://localhost:${env.port}/health`);
  console.log(`   API base:     http://localhost:${env.port}/api`);
});

export default app;
