import { LogOut, Shield, Store } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'

export function TopBar() {
  const { user, isAdmin, logout } = useAuth()

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6 shadow-sm">
      <h2 className="text-lg font-semibold text-gray-800">
        {isAdmin ? 'Admin Dashboard' : 'Partner Dashboard'}
      </h2>
      <div className="flex items-center gap-4">
        <span className="flex items-center gap-2 text-sm text-gray-600">
          {isAdmin ? (
            <span className="inline-flex items-center gap-1 rounded-full bg-purple-100 px-2 py-0.5 text-xs font-medium text-purple-700">
              <Shield className="h-3 w-3" />
              Admin
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">
              <Store className="h-3 w-3" />
              Partner
            </span>
          )}
          {user?.email}
        </span>
        <button
          onClick={logout}
          className="flex items-center gap-1 rounded-md px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
        >
          <LogOut className="h-4 w-4" />
          Logout
        </button>
      </div>
    </header>
  )
}
