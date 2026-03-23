import { Request, Response, NextFunction } from 'express';
import { AppError } from '../utils/errors';

export function errorHandler(err: Error, req: Request, res: Response, _next: NextFunction) {
  // Always log the full error with stack trace for debugging
  console.error(`[${req.method} ${req.path}] Error:`, err.message);
  console.error(err.stack);

  if (err instanceof AppError) {
    return res.status(err.statusCode).json({
      success: false,
      error: {
        code: err.code,
        message: err.message,
        details: err.details,
      },
    });
  }

  // In development, include the actual error message so you can debug
  const isDev = process.env.NODE_ENV !== 'production';

  return res.status(500).json({
    success: false,
    error: {
      code: 'INTERNAL_ERROR',
      message: isDev ? err.message : 'An unexpected error occurred',
      ...(isDev && { stack: err.stack }),
    },
  });
}
