import { Request, Response, NextFunction } from 'express';
import { getFirebaseAuth } from '../config/firebase';
import { query } from '../config/database';
import { UnauthorizedError } from '../utils/errors';

export interface AuthUser {
  id: number;
  firebaseUid: string;
  email: string;
  role: string;
  partnerType: string | null;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthUser;
    }
  }
}

export async function authenticate(req: Request, _res: Response, next: NextFunction) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith('Bearer ')) {
      throw new UnauthorizedError('Missing or invalid authorization header');
    }

    const token = authHeader.split('Bearer ')[1];
    const firebaseAuth = getFirebaseAuth();

    let firebaseUid: string;

    if (firebaseAuth) {
      const decoded = await firebaseAuth.verifyIdToken(token);
      firebaseUid = decoded.uid;
    } else {
      // Dev mode: treat token as firebase UID directly for testing
      firebaseUid = token;
    }

    const result = await query(
      'SELECT id, firebase_uid, email, role, partner_type FROM users WHERE firebase_uid = $1',
      [firebaseUid]
    );

    if (result.rows.length > 0) {
      const row = result.rows[0];
      req.user = {
        id: row.id,
        firebaseUid: row.firebase_uid,
        email: row.email,
        role: row.role,
        partnerType: row.partner_type,
      };
    } else {
      // User not yet registered — only firebase_uid is available
      req.user = {
        id: 0,
        firebaseUid,
        email: '',
        role: 'player',
        partnerType: null,
      };
    }

    next();
  } catch (error) {
    if (error instanceof UnauthorizedError) {
      next(error);
    } else {
      next(new UnauthorizedError('Invalid or expired token'));
    }
  }
}
