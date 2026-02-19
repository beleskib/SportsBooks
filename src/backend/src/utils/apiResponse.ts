import { Response } from 'express';

export function success<T>(res: Response, data: T, message?: string, statusCode = 200) {
  return res.status(statusCode).json({
    success: true,
    data,
    message,
  });
}

export function created<T>(res: Response, data: T, message?: string) {
  return success(res, data, message, 201);
}

export function paginated<T>(
  res: Response,
  data: T[],
  pagination: { page: number; limit: number; total: number }
) {
  return res.status(200).json({
    success: true,
    data,
    pagination: {
      ...pagination,
      totalPages: Math.ceil(pagination.total / pagination.limit),
    },
  });
}

export function error(
  res: Response,
  statusCode: number,
  code: string,
  message: string,
  details?: Record<string, string[]>
) {
  return res.status(statusCode).json({
    success: false,
    error: { code, message, details },
  });
}
