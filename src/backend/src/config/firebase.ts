import * as admin from 'firebase-admin';
import { env } from './env';
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
    console.log('Firebase Admin initialized');
  } else {
    console.warn(`Firebase service account not found at ${serviceAccountPath}. Auth verification disabled.`);
  }
} catch (error) {
  console.warn('Firebase Admin initialization failed:', error);
}

export const getFirebaseAuth = (): admin.auth.Auth | null => {
  return firebaseApp ? admin.auth(firebaseApp) : null;
};

export { firebaseApp };
