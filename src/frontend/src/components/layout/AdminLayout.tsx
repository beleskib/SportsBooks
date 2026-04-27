import { Outlet } from 'react-router-dom'
import { Clock } from 'lucide-react'
import { Sidebar } from './Sidebar'
import { TopBar } from './TopBar'
import { useAuth } from '@/context/AuthContext'

// Banner shown to partners whose account hasn't been approved by an admin yet.
// Read-only screens still work, but every "create / publish" action is gated
// server-side by requireApprovedPartner — so we tell them up front.
function PendingApprovalBanner() {
  const { isAdmin, isPartner, backendUser } = useAuth()
  if (isAdmin || !isPartner || !backendUser) return null
  if (backendUser.partnerApprovedAt) return null

  const reason = backendUser.partnerRejectionReason

  return (
    <div className="border-b border-yellow-200 bg-yellow-50 px-6 py-3">
      <div className="flex items-start gap-3">
        <Clock className="mt-0.5 h-5 w-5 text-yellow-700" />
        <div className="text-sm">
          <div className="font-semibold text-yellow-900">
            Your partner account is awaiting approval
          </div>
          <div className="mt-0.5 text-yellow-800">
            You can prepare drafts, but venues, coaches and time slots stay private until an
            admin approves your account. We'll email you the moment that happens.
          </div>
          {reason && (
            <div className="mt-2 rounded-md bg-yellow-100 p-2 text-yellow-900">
              <span className="font-medium">Asked to revise:</span> {reason}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export function AdminLayout() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar />
        <PendingApprovalBanner />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
