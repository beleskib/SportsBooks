import { useEffect, useState, useRef } from 'react'
import { Bell, Check, Clock, CheckCircle, XCircle, MessageSquare, Info } from 'lucide-react'
import { notificationApi, type Notification } from '@/api/notifications'

function notificationIcon(type: string) {
  switch (type) {
    case 'booking_request': return <Clock className="h-4 w-4 text-yellow-500" />
    case 'booking_approved': return <CheckCircle className="h-4 w-4 text-emerald-500" />
    case 'booking_declined': return <XCircle className="h-4 w-4 text-red-500" />
    case 'booking_confirmed': return <Check className="h-4 w-4 text-blue-500" />
    case 'chat_message': return <MessageSquare className="h-4 w-4 text-purple-500" />
    default: return <Info className="h-4 w-4 text-gray-400" />
  }
}

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  const days = Math.floor(hrs / 24)
  return `${days}d ago`
}

export function NotificationBell() {
  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Fetch unread count on mount and every 30 seconds
  useEffect(() => {
    const fetchCount = async () => {
      try {
        const res = await notificationApi.getUnreadCount()
        setUnreadCount(res.data.count)
      } catch {
        // silently fail
      }
    }
    fetchCount()
    const interval = setInterval(fetchCount, 30000)
    return () => clearInterval(interval)
  }, [])

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const toggleOpen = async () => {
    if (!open) {
      setLoading(true)
      try {
        const res = await notificationApi.getAll(20, 0)
        setNotifications(res.data)
      } catch {
        setNotifications([])
      } finally {
        setLoading(false)
      }
    }
    setOpen(!open)
  }

  const markAllRead = async () => {
    const unreadIds = notifications.filter(n => !n.isRead).map(n => n.id)
    if (unreadIds.length === 0) return
    try {
      await notificationApi.markAsRead(unreadIds)
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
      setUnreadCount(0)
    } catch {
      // silently fail
    }
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={toggleOpen}
        className="relative rounded-md p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-full z-50 mt-2 w-96 rounded-xl border border-gray-200 bg-white shadow-xl">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h3 className="text-sm font-semibold text-gray-900">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="text-xs font-medium text-blue-600 hover:text-blue-800"
              >
                Mark all as read
              </button>
            )}
          </div>

          {/* List */}
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-gray-600" />
              </div>
            ) : notifications.length === 0 ? (
              <div className="py-8 text-center">
                <Bell className="mx-auto h-8 w-8 text-gray-200" />
                <p className="mt-2 text-sm text-gray-400">No notifications yet</p>
              </div>
            ) : (
              notifications.map(n => (
                <div
                  key={n.id}
                  className={`flex gap-3 border-b border-gray-50 px-4 py-3 transition-colors hover:bg-gray-50 ${
                    !n.isRead ? 'bg-blue-50/50' : ''
                  }`}
                >
                  <div className="mt-0.5 flex-shrink-0">
                    {notificationIcon(n.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm ${!n.isRead ? 'font-medium text-gray-900' : 'text-gray-700'}`}>
                      {n.title}
                    </p>
                    <p className="mt-0.5 text-xs text-gray-500 truncate">{n.body}</p>
                    <p className="mt-1 text-xs text-gray-400">{timeAgo(n.createdAt)}</p>
                  </div>
                  {!n.isRead && (
                    <div className="mt-1 flex-shrink-0">
                      <div className="h-2 w-2 rounded-full bg-blue-500" />
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
