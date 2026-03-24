import { useEffect, useState } from 'react'
import { CheckCircle, XCircle, X } from 'lucide-react'

export type ToastVariant = 'success' | 'error'

interface ToastProps {
  message: string
  variant?: ToastVariant
  duration?: number
  onDismiss: () => void
}

export function Toast({ message, variant = 'success', duration = 3500, onDismiss }: ToastProps) {
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false)
      setTimeout(onDismiss, 300)
    }, duration)
    return () => clearTimeout(timer)
  }, [duration, onDismiss])

  const base =
    'fixed bottom-6 right-6 z-50 flex items-start gap-3 rounded-lg px-4 py-3 shadow-lg transition-all duration-300'
  const variantStyles: Record<ToastVariant, string> = {
    success: 'bg-green-50 text-green-800 border border-green-200',
    error: 'bg-red-50 text-red-800 border border-red-200',
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className={`${base} ${variantStyles[variant]} ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'}`}
    >
      {variant === 'success' ? (
        <CheckCircle className="mt-0.5 h-5 w-5 shrink-0 text-green-600" aria-hidden="true" />
      ) : (
        <XCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" aria-hidden="true" />
      )}
      <p className="text-sm font-medium">{message}</p>
      <button
        type="button"
        onClick={() => {
          setVisible(false)
          setTimeout(onDismiss, 300)
        }}
        aria-label="Dismiss notification"
        className="ml-2 rounded p-0.5 hover:bg-black/10"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  )
}
