import * as admin from 'firebase-admin';
import { env } from './env';
import { logger } from './logger';
import * as fs from 'fs';
import * as path from 'path';

let firebaseApp: admin.app.App | null = null;

try {
  const serviceAccountPath = path.resolve(env.firebaseServiceAccountPath);
  if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));
    firebaseApp = admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });
    logger.info('Firebase Admin initialized');
  } else {
    logger.warn(`Firebase service account not found at ${serviceAccountPath}. Auth verification disabled.`);
  }
} catch (error) {
  if (process.env.NODE_ENV === 'production') {
    logger.error('FATAL: Firebase initialization failed');
    process.exit(1);
  }
  logger.warn(error, 'Firebase Admin initialization failed');
}

export const getFirebaseAuth = (): admin.auth.Auth | null => {
  return firebaseApp ? admin.auth(firebaseApp) : null;
};

export const getFirestoreDb = (): admin.firestore.Firestore | null => {
  return firebaseApp ? admin.firestore(firebaseApp) : null;
};

export const getFirebaseMessaging = (): admin.messaging.Messaging | null => {
  return firebaseApp ? admin.messaging(firebaseApp) : null;
};

export { firebaseApp };
