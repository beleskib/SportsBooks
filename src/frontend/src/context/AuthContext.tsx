import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import {
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
  type User as FirebaseUser,
} from 'firebase/auth'
import { auth } from '@/config/firebase'
import { userApi } from '@/api/users'
import type { BackendUser, PartnerType } from '@/types'
import { UserRole } from '@/types'

interface AuthContextType {
  user: FirebaseUser | null
  backendUser: BackendUser | null
  isAdmin: boolean
  isPartner: boolean
  partnerType: PartnerType | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<FirebaseUser | null>(null)
  const [backendUser, setBackendUser] = useState<BackendUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      setUser(firebaseUser)
      if (firebaseUser) {
        try {
          const res = await userApi.getMe()
          setBackendUser(res.data)
        } catch {
          setBackendUser(null)
        }
      } else {
        setBackendUser(null)
      }
      setLoading(false)
    })
    return unsubscribe
  }, [])

  const isAdmin = backendUser?.role === UserRole.ADMIN
  const isPartner = backendUser?.role === UserRole.PARTNER
  const partnerType = backendUser?.partnerType ?? null

  const login = async (email: string, password: string) => {
    await signInWithEmailAndPassword(auth, email, password)
  }

  const logout = async () => {
    await signOut(auth)
    setBackendUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, backendUser, isAdmin, isPartner, partnerType, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
