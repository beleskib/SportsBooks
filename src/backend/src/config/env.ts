function requireEnv(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(
      `Missing required environment variable: ${name}. ` +
        `Set it in src/backend/.env (see .env.example).`,
    );
  }
  return value;
}

export const env = {
  port: parseInt(process.env.PORT || '3000', 10),
  databaseUrl: requireEnv('DATABASE_URL'),
  firebaseServiceAccountPath: requireEnv('FIREBASE_SERVICE_ACCOUNT_PATH'),
  stripeSecretKey: requireEnv('STRIPE_SECRET_KEY'),
  stripeWebhookSecret: requireEnv('STRIPE_WEBHOOK_SECRET'),
  stripeConnectReturnUrl:
    process.env.STRIPE_CONNECT_RETURN_URL || 'http://localhost:5173/stripe?status=return',
  stripeConnectRefreshUrl:
    process.env.STRIPE_CONNECT_REFRESH_URL || 'http://localhost:5173/stripe?status=refresh',
};
