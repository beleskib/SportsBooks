import rateLimit from 'express-rate-limit';

// Rate limits are relaxed in development/test to avoid blocking rapid
// iteration and automated test suites, while production keeps strict
// limits to guard against brute-force and abuse.
const isDev = process.env.NODE_ENV !== 'production';

// Auth endpoint limiter: 1000 req/15 min (dev/test) | 5 req/15 min (prod)
export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: isDev ? 1000 : 5,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    error: 'Too many authentication attempts. Please try again in 15 minutes.',
  },
});

// General API limiter: 10000 req/15 min (dev/test) | 100 req/15 min (prod)
export const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: isDev ? 10000 : 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    error: 'Too many requests. Please try again later.',
  },
});
