import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getStorage } from 'firebase/storage'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'AIzaSyDoaoZe629bB46-jF5L4WHvOBXg0rZk6OY',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || 'sportsbookings-988c1.firebaseapp.com',
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || 'sportsbookings-988c1',
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || 'sportsbookings-988c1.firebasestorage.app',
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '386257770001',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '',
}

const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const storage = getStorage(app)
