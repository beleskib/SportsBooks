import { useEffect, useState, useRef } from 'react'
import { Mail, User } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { dmApi, type ConversationPreview } from '@/api/dm'

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

export function InboxBell() {
  const [open, setOpen] = useState(false)
  const [conversations, setConversations] = useState<ConversationPreview[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  // Fetch unread count on mount and every 30 seconds
  useEffect(() => {
    const fetchCount = async () => {
      try {
        const res = await dmApi.getUnreadCount()
        setUnreadCount(res.data.unreadCount)
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
        const res = await dmApi.getConversations()
        setConversations(res.data)
      } catch {
        setConversations([])
      } finally {
        setLoading(false)
      }
    }
    setOpen(!open)
  }

  const openConversation = (friendUserId: number) => {
    setOpen(false)
    navigate(`/inbox/${friendUserId}`)
  }

  const openInbox = () => {
    setOpen(false)
    navigate('/inbox')
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={toggleOpen}
        className="relative rounded-md p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
        title="Messages"
      >
        <Mail className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-emerald-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-full z-50 mt-2 w-96 rounded-xl border border-gray-200 bg-white shadow-xl">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h3 className="text-sm font-semibold text-gray-900">Messages</h3>
            <button
              onClick={openInbox}
              className="text-xs font-medium text-blue-600 hover:text-blue-800"
            >
              View all
            </button>
          </div>

          {/* List */}
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-gray-600" />
              </div>
            ) : conversations.length === 0 ? (
              <div className="py-8 text-center">
                <Mail className="mx-auto h-8 w-8 text-gray-200" />
                <p className="mt-2 text-sm text-gray-400">No conversations yet</p>
              </div>
            ) : (
              conversations.map((c) => (
                <button
                  key={c.friendUserId}
                  onClick={() => openConversation(c.friendUserId)}
                  className={`flex w-full items-start gap-3 border-b border-gray-50 px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
                    c.unreadCount > 0 ? 'bg-emerald-50/50' : ''
                  }`}
                >
                  {/* Avatar */}
                  <div className="flex-shrink-0">
                    {c.friendPhotoUrl ? (
                      <img
                        src={c.friendPhotoUrl}
                        alt={c.friendDisplayName ?? 'User'}
                        className="h-10 w-10 rounded-full object-cover"
                      />
                    ) : (
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-200">
                        <User className="h-5 w-5 text-gray-500" />
                      </div>
                    )}
                  </div>

                  {/* Content */}
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between">
                      <p className={`text-sm ${c.unreadCount > 0 ? 'font-semibold text-gray-900' : 'font-medium text-gray-700'}`}>
                        {c.friendDisplayName ?? `User #${c.friendUserId}`}
                      </p>
                      <span className="ml-2 flex-shrink-0 text-xs text-gray-400">
                        {timeAgo(c.lastMessageAt)}
                      </span>
                    </div>
                    <p className={`mt-0.5 truncate text-xs ${c.unreadCount > 0 ? 'font-medium text-gray-700' : 'text-gray-500'}`}>
                      {c.lastMessage}
                    </p>
                  </div>

                  {/* Unread badge */}
                  {c.unreadCount > 0 && (
                    <div className="flex-shrink-0 pt-1">
                      <span className="flex h-5 min-w-[20px] items-center justify-center rounded-full bg-emerald-500 px-1.5 text-[10px] font-bold text-white">
                        {c.unreadCount > 99 ? '99+' : c.unreadCount}
                      </span>
                    </div>
                  )}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
