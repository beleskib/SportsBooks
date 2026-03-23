import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  MapPin,
  Users,
  CreditCard,
  DollarSign,
  Calendar,
} from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { PartnerType } from '@/types'

export function Sidebar() {
  const { isAdmin, partnerType } = useAuth()

  const adminNavItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/venues', icon: MapPin, label: 'All Venues' },
    { to: '/coaches', icon: Users, label: 'All Coaches' },
    { to: '/earnings', icon: DollarSign, label: 'Revenue' },
  ]

  const partnerNavItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    ...(partnerType === PartnerType.VENUE_OWNER
      ? [{ to: '/venues', icon: MapPin, label: 'My Venues' }]
      : []),
    ...(partnerType === PartnerType.COACH
      ? [{ to: '/coaches', icon: Users, label: 'My Profile' }]
      : []),
    { to: '/time-slots', icon: Calendar, label: 'Time Slots' },
    { to: '/earnings', icon: DollarSign, label: 'Earnings' },
    { to: '/stripe', icon: CreditCard, label: 'Payment Setup' },
  ]

  const navItems = isAdmin ? adminNavItems : partnerNavItems

  return (
    <aside className="flex w-64 flex-col bg-gray-900 text-white">
      <div className="flex h-16 items-center px-6">
        <h1 className="text-xl font-bold">SportsBooks</h1>
      </div>
      <nav className="mt-2 flex-1 space-y-1 px-3">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-gray-700 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <item.icon className="h-5 w-5" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-gray-700 p-4">
        <p className="text-xs text-gray-400">
          {isAdmin ? 'Admin Panel' : 'Partner Dashboard'}
        </p>
      </div>
    </aside>
  )
}
