import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { TopBar } from './TopBar'

// Layout shell for the authenticated app. Partner accounts are no longer
// gated at the account level — approval now happens per listing (venue/coach),
// so partners get the full UI from day one and individual listings stay
// private until an admin approves them. See AdminListingsPage for the queue.
export function AdminLayout() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
