import { useEffect, useState, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Mail, User, Send, ArrowLeft, MessageSquare } from 'lucide-react'
import { dmApi, type ConversationPreview, type DirectMessage } from '@/api/dm'
import { useAuth } from '@/context/AuthContext'

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  const days = Math.floor(hrs / 24)
  if (days < 7) return `${days}d ago`
  return new Date(dateStr).toLocaleDateString()
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)

  if (d.toDateString() === today.toDateString()) return 'Today'
  if (d.toDateString() === yesterday.toDateString()) return 'Yesterday'
  return d.toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' })
}

// ── Conversation list (left panel) ────────────────────────────

interface ConversationListProps {
  conversations: ConversationPreview[]
  selectedId: number | null
  onSelect: (friendUserId: number) => void
  loading: boolean
}

function ConversationList({ conversations, selectedId, onSelect, loading }: ConversationListProps) {
  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <div className="h-6 w-6 animate-spin rounded-full border-2 border-gray-300 border-t-gray-600" />
      </div>
    )
  }

  if (conversations.length === 0) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center px-4 text-center">
        <Mail className="h-12 w-12 text-gray-200" />
        <p className="mt-3 text-sm font-medium text-gray-500">No conversations yet</p>
        <p className="mt-1 text-xs text-gray-400">
          Messages from friends will appear here
        </p>
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-y-auto">
      {conversations.map((c) => (
        <button
          key={c.friendUserId}
          onClick={() => onSelect(c.friendUserId)}
          className={`flex w-full items-start gap-3 border-b border-gray-100 px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
            selectedId === c.friendUserId
              ? 'bg-blue-50 border-l-2 border-l-blue-500'
              : c.unreadCount > 0
              ? 'bg-emerald-50/40'
              : ''
          }`}
        >
          {/* Avatar */}
          {c.friendPhotoUrl ? (
            <img
              src={c.friendPhotoUrl}
              alt={c.friendDisplayName ?? 'User'}
              className="h-10 w-10 flex-shrink-0 rounded-full object-cover"
            />
          ) : (
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-gray-200">
              <User className="h-5 w-5 text-gray-500" />
            </div>
          )}

          {/* Text */}
          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between">
              <p className={`text-sm ${c.unreadCount > 0 ? 'font-semibold text-gray-900' : 'font-medium text-gray-700'}`}>
                {c.friendDisplayName ?? `User #${c.friendUserId}`}
              </p>
              <span className="ml-2 flex-shrink-0 text-[11px] text-gray-400">
                {timeAgo(c.lastMessageAt)}
              </span>
            </div>
            <p className={`mt-0.5 truncate text-xs ${c.unreadCount > 0 ? 'font-medium text-gray-600' : 'text-gray-400'}`}>
              {c.lastMessage}
            </p>
          </div>

          {c.unreadCount > 0 && (
            <span className="mt-1 flex h-5 min-w-[20px] flex-shrink-0 items-center justify-center rounded-full bg-emerald-500 px-1.5 text-[10px] font-bold text-white">
              {c.unreadCount > 99 ? '99+' : c.unreadCount}
            </span>
          )}
        </button>
      ))}
    </div>
  )
}

// ── Message thread (right panel) ──────────────────────────────

interface MessageThreadProps {
  friendUserId: number
  friendName: string
  friendPhotoUrl: string | null
  onBack: () => void
}

function MessageThread({ friendUserId, friendName, friendPhotoUrl, onBack }: MessageThreadProps) {
  const { backendUser } = useAuth()
  const [messages, setMessages] = useState<DirectMessage[]>([])
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [newMessage, setNewMessage] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [])

  // Load messages
  useEffect(() => {
    let cancelled = false
    const loadMessages = async () => {
      setLoading(true)
      try {
        const res = await dmApi.getMessages(friendUserId)
        if (!cancelled) {
          setMessages(res.data)
        }
        // Mark as read (fire-and-forget)
        dmApi.markRead(friendUserId).catch(() => {})
      } catch {
        if (!cancelled) setMessages([])
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadMessages()
    // Poll for new messages every 10 seconds
    const interval = setInterval(async () => {
      try {
        const res = await dmApi.getMessages(friendUserId)
        if (!cancelled) setMessages(res.data)
        dmApi.markRead(friendUserId).catch(() => {})
      } catch { /* ignore */ }
    }, 10000)
    return () => { cancelled = true; clearInterval(interval) }
  }, [friendUserId])

  // Scroll on new messages
  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  // Focus input on mount
  useEffect(() => {
    inputRef.current?.focus()
  }, [friendUserId])

  const handleSend = async () => {
    const text = newMessage.trim()
    if (!text || sending) return
    setSending(true)
    try {
      const res = await dmApi.sendMessage(friendUserId, text)
      setMessages((prev) => [...prev, res.data])
      setNewMessage('')
    } catch {
      // TODO: show toast
    } finally {
      setSending(false)
      inputRef.current?.focus()
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  // Group messages by day for date separators
  const groupedMessages: { date: string; msgs: DirectMessage[] }[] = []
  for (const msg of messages) {
    const dateKey = new Date(msg.createdAt).toDateString()
    const last = groupedMessages[groupedMessages.length - 1]
    if (last && last.date === dateKey) {
      last.msgs.push(msg)
    } else {
      groupedMessages.push({ date: dateKey, msgs: [msg] })
    }
  }

  return (
    <div className="flex flex-1 flex-col">
      {/* Thread header */}
      <div className="flex items-center gap-3 border-b border-gray-200 bg-white px-4 py-3">
        <button
          onClick={onBack}
          className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 lg:hidden"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        {friendPhotoUrl ? (
          <img src={friendPhotoUrl} alt={friendName} className="h-9 w-9 rounded-full object-cover" />
        ) : (
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gray-200">
            <User className="h-4 w-4 text-gray-500" />
          </div>
        )}
        <div>
          <p className="text-sm font-semibold text-gray-900">{friendName}</p>
        </div>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto bg-gray-50 px-4 py-4">
        {loading ? (
          <div className="flex h-full items-center justify-center">
            <div className="h-6 w-6 animate-spin rounded-full border-2 border-gray-300 border-t-gray-600" />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex h-full flex-col items-center justify-center text-center">
            <MessageSquare className="h-10 w-10 text-gray-200" />
            <p className="mt-3 text-sm text-gray-400">No messages yet. Say hello!</p>
          </div>
        ) : (
          groupedMessages.map((group) => (
            <div key={group.date}>
              {/* Date separator */}
              <div className="my-4 flex items-center gap-3">
                <div className="flex-1 border-t border-gray-200" />
                <span className="text-[11px] font-medium text-gray-400">
                  {formatDate(group.msgs[0].createdAt)}
                </span>
                <div className="flex-1 border-t border-gray-200" />
              </div>
              {/* Messages */}
              {group.msgs.map((msg) => {
                const isMine = msg.senderId === backendUser?.id
                return (
                  <div
                    key={msg.id}
                    className={`mb-2 flex ${isMine ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-[70%] rounded-2xl px-4 py-2 ${
                        isMine
                          ? 'bg-blue-500 text-white'
                          : 'bg-white text-gray-900 shadow-sm border border-gray-100'
                      }`}
                    >
                      <p className="text-sm whitespace-pre-wrap break-words">{msg.message}</p>
                      <p className={`mt-1 text-right text-[10px] ${isMine ? 'text-blue-100' : 'text-gray-400'}`}>
                        {formatTime(msg.createdAt)}
                      </p>
                    </div>
                  </div>
                )
              })}
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input bar */}
      <div className="border-t border-gray-200 bg-white px-4 py-3">
        <div className="flex items-center gap-2">
          <input
            ref={inputRef}
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type a message..."
            className="flex-1 rounded-full border border-gray-300 bg-gray-50 px-4 py-2 text-sm outline-none focus:border-blue-400 focus:ring-1 focus:ring-blue-400"
            disabled={sending}
          />
          <button
            onClick={handleSend}
            disabled={!newMessage.trim() || sending}
            className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-500 text-white transition-colors hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            <Send className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  )
}

// ── Main page ─────────────────────────────────────────────────

export function InboxPage() {
  const { friendId } = useParams<{ friendId?: string }>()
  const navigate = useNavigate()
  const [conversations, setConversations] = useState<ConversationPreview[]>([])
  const [loading, setLoading] = useState(true)

  const selectedFriendId = friendId ? Number(friendId) : null

  // Load conversations
  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const res = await dmApi.getConversations()
        if (!cancelled) setConversations(res.data)
      } catch {
        if (!cancelled) setConversations([])
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    // Refresh conversation list every 30 seconds
    const interval = setInterval(async () => {
      try {
        const res = await dmApi.getConversations()
        if (!cancelled) setConversations(res.data)
      } catch { /* ignore */ }
    }, 30000)
    return () => { cancelled = true; clearInterval(interval) }
  }, [])

  const selectedConversation = conversations.find((c) => c.friendUserId === selectedFriendId)

  return (
    <div className="flex h-[calc(100vh-7rem)] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
      {/* Left panel — conversation list */}
      <div
        className={`flex w-full flex-col border-r border-gray-200 lg:w-80 ${
          selectedFriendId ? 'hidden lg:flex' : 'flex'
        }`}
      >
        <div className="flex items-center gap-2 border-b border-gray-200 px-4 py-3">
          <Mail className="h-5 w-5 text-gray-500" />
          <h2 className="text-sm font-semibold text-gray-900">Inbox</h2>
          {conversations.filter((c) => c.unreadCount > 0).length > 0 && (
            <span className="ml-auto flex h-5 min-w-[20px] items-center justify-center rounded-full bg-emerald-500 px-1.5 text-[10px] font-bold text-white">
              {conversations.reduce((sum, c) => sum + c.unreadCount, 0)}
            </span>
          )}
        </div>
        <ConversationList
          conversations={conversations}
          selectedId={selectedFriendId}
          onSelect={(id) => navigate(`/inbox/${id}`)}
          loading={loading}
        />
      </div>

      {/* Right panel — message thread or empty state */}
      {selectedFriendId && selectedConversation ? (
        <MessageThread
          key={selectedFriendId}
          friendUserId={selectedFriendId}
          friendName={selectedConversation.friendDisplayName ?? `User #${selectedFriendId}`}
          friendPhotoUrl={selectedConversation.friendPhotoUrl}
          onBack={() => navigate('/inbox')}
        />
      ) : selectedFriendId && !loading ? (
        // Friend ID in URL but not in conversation list — show thread anyway
        <MessageThread
          key={selectedFriendId}
          friendUserId={selectedFriendId}
          friendName={`User #${selectedFriendId}`}
          friendPhotoUrl={null}
          onBack={() => navigate('/inbox')}
        />
      ) : (
        <div
          className={`flex flex-1 flex-col items-center justify-center text-center ${
            selectedFriendId ? '' : 'hidden lg:flex'
          }`}
        >
          <MessageSquare className="h-16 w-16 text-gray-200" />
          <p className="mt-4 text-lg font-medium text-gray-400">Select a conversation</p>
          <p className="mt-1 text-sm text-gray-300">
            Choose a conversation from the left to start messaging
          </p>
        </div>
      )}
    </div>
  )
}
