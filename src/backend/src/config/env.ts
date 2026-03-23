export const env = {
  port: parseInt(process.env.PORT || '3000', 10),
  databaseUrl: process.env.DATABASE_URL || 'postgresql://postgres:password@localhost:5432/sportsbook',
  firebaseServiceAccountPath: process.env.FIREBASE_SERVICE_ACCOUNT_PATH || './firebase-service-account.json',
  stripeSecretKey: process.env.STRIPE_SECRET_KEY || '',
  stripeWebhookSecret: process.env.STRIPE_WEBHOOK_SECRET || '',
  stripeConnectReturnUrl: process.env.STRIPE_CONNECT_RETURN_URL || 'http://localhost:5173/stripe?status=return',
  stripeConnectRefreshUrl: process.env.STRIPE_CONNECT_REFRESH_URL || 'http://localhost:5173/stripe?status=refresh',
};
