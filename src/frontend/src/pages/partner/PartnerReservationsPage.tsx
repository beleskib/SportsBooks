import { useCallback, useEffect, useState, useRef } from 'react'
import {
  CheckCircle,
  XCircle,
  Clock,
  MessageSquare,
  Calendar,
  User,
  MapPin,
  Send,
  X,
  Inbox,
} from 'lucide-react'
import { bookingApi, type ReservationDetail, type BookingMessage } from '@/api/bookings'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

type StatusTab = 'all' | 'pending' | 'approved' | 'confirmed' | 'completed' | 'cancelled'

const STATUS_TABS: { key: StatusTab; label: string }[] = [
  { key: 'all', label: 'All' },
  { key: 'pending', label: 'Pending' },
  { key: 'approved', label: 'Approved' },
  { key: 'confirmed', label: 'Confirmed' },
  { key: 'completed', label: 'Completed' },
  { key: 'cancelled', label: 'Cancelled' },
]

function statusBadge(status: string) {
  const styles: Record<string, string> = {
    pending: 'bg-yellow-100 text-yellow-800',
    approved: 'bg-blue-100 text-blue-800',
    confirmed: 'bg-emerald-100 text-emerald-800',
    completed: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800',
  }
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${styles[status] ?? 'bg-gray-100 text-gray-800'}`}>
      {status}
    </span>
  )
}

function formatSlot(r: ReservationDetail): string {
  const slot = r.timeSlot
  if (!slot) return 'Date TBC'
  const [y, m, d] = slot.slotDate.split('-').map(Number)
  const dt = new Date(Date.UTC(y, m - 1, d))
  const datePart = dt.toLocaleDateString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short', timeZone: 'UTC',
  })
  return `${datePart} · ${slot.startTime?.substring(0, 5) ?? ''}–${slot.endTime?.substring(0, 5) ?? ''}`
}

export function PartnerReservationsPage() {
  const [reservations, setReservations] = useState<ReservationDetail[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<StatusTab>('all')
  const [actionLoading, setActionLoading] = useState<number | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  // Chat drawer state
  const [chatOpen, setChatOpen] = useState(false)
  const [chatBookingId, setChatBookingId] = useState<number | null>(null)
  const [chatBookingName, setChatBookingName] = useState('')
  const [messages, setMessages] = useState<BookingMessage[]>([])
  const [chatLoading, setChatLoading] = useState(false)
  const [newMessage, setNewMessage] = useState('')
  const [sendingMessage, setSendingMessage] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const fetchReservations = useCallback(async () => {
    try {
      setLoading(true)
      const status = activeTab === 'all' ? undefined : activeTab
      const res = await bookingApi.getPartnerBookings(status)
      setReservations(res.data)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to load reservations'
      setError(message)
    } finally {
      setLoading(false)
    }
  }, [activeTab])

  useEffect(() => {
    fetchReservations()
  }, [fetchReservations])

  // Auto-dismiss toast
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000)
      return () => clearTimeout(timer)
    }
  }, [toast])

  // Scroll chat to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleAction = async (id: number, action: 'approve' | 'decline') => {
    setActionLoading(id)
    try {
      if (action === 'approve') {
        await bookingApi.approve(id)
        setToast('Reservation approved — player notified')
      } else {
        await bookingApi.decline(id)
        setToast('Reservation declined — player notified')
      }
      // Refresh list
      await fetchReservations()
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Action failed'
      setToast(message)
    } finally {
      setActionLoading(null)
    }
  }

  const openChat = async (booking: ReservationDetail) => {
    setChatBookingId(booking.id)
    setChatBookingName(booking.venue?.name ?? booking.coach?.name ?? `Booking #${booking.id}`)
    setChatOpen(true)
    setChatLoading(true)
    try {
      const res = await bookingApi.getMessages(booking.id)
      setMessages(res.data)
    } catch {
      setMessages([])
    } finally {
      setChatLoading(false)
    }
  }

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !chatBookingId) return
    setSendingMessage(true)
    try {
      await bookingApi.sendMessage(chatBookingId, newMessage.trim())
      setNewMessage('')
      // Refresh messages
      const res = await bookingApi.getMessages(chatBookingId)
      setMessages(res.data)
    } catch {
      setToast('Failed to send message')
    } finally {
      setSendingMessage(false)
    }
  }

  const pendingCount = reservations.filter(r => r.status === 'pending').length

  return (
    <div className="relative">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Reservations</h2>
          <p className="mt-1 text-sm text-gray-500">
            Manage booking requests from players
          </p>
        </div>
        {pendingCount > 0 && (
          <div className="flex items-center gap-2 rounded-lg bg-yellow-50 px-4 py-2 border border-yellow-200">
            <Clock className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-yellow-800">
              {pendingCount} pending request{pendingCount !== 1 ? 's' : ''}
            </span>
          </div>
        )}
      </div>

      {/* Status tabs */}
      <div className="mb-6 flex gap-1 rounded-lg bg-gray-100 p-1">
        {STATUS_TABS.map(tab => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
              activeTab === tab.key
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Toast */}
      {toast && (
        <div className="mb-4 rounded-lg bg-gray-900 px-4 py-3 text-sm font-medium text-white shadow-lg">
          {toast}
        </div>
      )}

      {/* Content */}
      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <div className="rounded-lg bg-red-50 p-4 text-sm text-red-700">{error}</div>
      ) : reservations.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-200 py-16">
          <Inbox className="h-12 w-12 text-gray-300" />
          <p className="mt-3 text-sm font-medium text-gray-500">
            No {activeTab === 'all' ? '' : activeTab} reservations
          </p>
          <p className="mt-1 text-xs text-gray-400">
            {activeTab === 'pending'
              ? 'All caught up! No pending requests.'
              : 'Reservations will appear here when players book your services.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {reservations.map(r => (
            <div
              key={r.id}
              className={`rounded-xl border bg-white p-5 shadow-sm transition-colors ${
                r.status === 'pending' ? 'border-yellow-200 bg-yellow-50/30' : 'border-gray-200'
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <h3 className="text-sm font-semibold text-gray-900">
                      {r.venue?.name ?? r.coach?.name ?? `Booking #${r.id}`}
                    </h3>
                    {statusBadge(r.status)}
                  </div>

                  <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-gray-500">
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3.5 w-3.5" />
                      {formatSlot(r)}
                    </span>
                    <span className="flex items-center gap-1">
                      <User className="h-3.5 w-3.5" />
                      {r.playerName ?? r.playerEmail ?? `Player #${r.playerId}`}
                    </span>
                    {r.venue?.address && (
                      <span className="flex items-center gap-1">
                        <MapPin className="h-3.5 w-3.5" />
                        {r.venue.address}
                      </span>
                    )}
                  </div>

                  {r.notes && (
                    <div className="mt-2 rounded-md bg-gray-50 px-3 py-2 text-xs text-gray-600">
                      <span className="font-medium text-gray-500">Note:</span> {r.notes}
                    </div>
                  )}
                </div>

                <div className="ml-4 text-right">
                  <div className="text-lg font-bold text-gray-900">{r.totalPrice.toFixed(0)} ден</div>
                  <div className="text-xs text-gray-400">
                    {new Date(r.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}
                  </div>
                </div>
              </div>

              {/* Action row */}
              <div className="mt-4 flex items-center gap-2 border-t border-gray-100 pt-3">
                {r.status === 'pending' && (
                  <>
                    <button
                      onClick={() => handleAction(r.id, 'approve')}
                      disabled={actionLoading === r.id}
                      className="flex items-center gap-1.5 rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-yellow-300 hover:bg-gray-800 disabled:opacity-50"
                    >
                      <CheckCircle className="h-4 w-4" />
                      {actionLoading === r.id ? 'Processing...' : 'Approve'}
                    </button>
                    <button
                      onClick={() => handleAction(r.id, 'decline')}
                      disabled={actionLoading === r.id}
                      className="flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                    >
                      <XCircle className="h-4 w-4" />
                      Decline
                    </button>
                  </>
                )}

                {/* Chat button — available for approved, confirmed, completed bookings */}
                {['approved', 'confirmed', 'completed'].includes(r.status) && (
                  <button
                    onClick={() => openChat(r)}
                    className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  >
                    <MessageSquare className="h-4 w-4" />
                    Chat
                  </button>
                )}

                <span className="ml-auto text-xs text-gray-400">
                  #{r.id}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Chat Drawer (slide-in from right) */}
      {chatOpen && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-40 bg-black/20 backdrop-blur-sm"
            onClick={() => setChatOpen(false)}
          />
          {/* Drawer */}
          <div className="fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col bg-white shadow-2xl">
            {/* Chat header */}
            <div className="flex items-center justify-between border-b border-gray-200 px-4 py-3">
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Chat</h3>
                <p className="text-xs text-gray-500">{chatBookingName} · Booking #{chatBookingId}</p>
              </div>
              <button
                onClick={() => setChatOpen(false)}
                className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {chatLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner />
                </div>
              ) : messages.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <MessageSquare className="h-10 w-10 text-gray-200" />
                  <p className="mt-2 text-sm text-gray-400">No messages yet</p>
                  <p className="text-xs text-gray-300">Start a conversation with the player</p>
                </div>
              ) : (
                messages.map(msg => (
                  <div key={msg.id} className="flex flex-col">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-medium text-gray-700">{msg.senderName}</span>
                      <span className="text-xs text-gray-400">
                        {new Date(msg.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="mt-1 rounded-lg bg-gray-100 px-3 py-2 text-sm text-gray-800">
                      {msg.content}
                    </div>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Message input */}
            <div className="border-t border-gray-200 p-3">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={newMessage}
                  onChange={e => setNewMessage(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && !e.shiftKey && handleSendMessage()}
                  placeholder="Type a message..."
                  className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-gray-500 focus:outline-none focus:ring-1 focus:ring-gray-500"
                />
                <button
                  onClick={handleSendMessage}
                  disabled={sendingMessage || !newMessage.trim()}
                  className="rounded-lg bg-gray-900 px-3 py-2 text-white hover:bg-gray-800 disabled:opacity-50"
                >
                  <Send className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default PartnerReservationsPage
