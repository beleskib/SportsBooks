import { useEffect, useState, useCallback } from 'react'
import { subscriptionApi } from '@/api/subscription'
import { Crown, Shield, Eye, EyeOff, Users, Check, Loader2, X } from 'lucide-react'
import type { ProfileVisibility } from '@shared/types/subscription'

interface SubState {
  isPlus: boolean
  subscription: {
    status: string
    currentPeriodEnd: string
    cancelAtPeriodEnd: boolean
    trialEnd: string | null
  } | null
  profileVisibility: ProfileVisibility
}

export function SettingsPage() {
  const [sub, setSub] = useState<SubState | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const fetchStatus = useCallback(async () => {
    try {
      const res = await subscriptionApi.getStatus()
      setSub(res.data)
    } catch {
      setError('Failed to load subscription status')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchStatus()
  }, [fetchStatus])

  // Check for ?plus=success/cancel in URL
  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    if (params.get('plus') === 'success') {
      setToast('Welcome to SportsBooks+! Your trial has started.')
      window.history.replaceState({}, '', window.location.pathname)
      fetchStatus()
    } else if (params.get('plus') === 'cancel') {
      setToast('Checkout was cancelled.')
      window.history.replaceState({}, '', window.location.pathname)
    }
  }, [fetchStatus])

  const handleUpgrade = async () => {
    setActionLoading(true)
    try {
      const res = await subscriptionApi.createCheckout()
      window.location.href = res.data.checkoutUrl
    } catch {
      setError('Failed to start checkout')
    } finally {
      setActionLoading(false)
    }
  }

  const handleCancel = async () => {
    if (!confirm("Cancel your SportsBooks+ subscription? You'll keep access until the end of the billing period.")) return
    setActionLoading(true)
    try {
      await subscriptionApi.cancel()
      setToast('Subscription will cancel at end of period')
      fetchStatus()
    } catch {
      setError('Failed to cancel subscription')
    } finally {
      setActionLoading(false)
    }
  }

  const handleReactivate = async () => {
    setActionLoading(true)
    try {
      await subscriptionApi.reactivate()
      setToast('Subscription reactivated!')
      fetchStatus()
    } catch {
      setError('Failed to reactivate')
    } finally {
      setActionLoading(false)
    }
  }

  const handleVisibilityChange = async (visibility: ProfileVisibility) => {
    setActionLoading(true)
    try {
      await subscriptionApi.setVisibility(visibility)
      setSub((prev) => (prev ? { ...prev, profileVisibility: visibility } : prev))
      setToast(`Profile visibility set to ${visibility.replace('_', ' ')}`)
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message || 'Failed to update visibility'
      setError(msg)
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  const isPlus = sub?.isPlus ?? false
  const isCanceling = sub?.subscription?.cancelAtPeriodEnd ?? false
  const periodEnd = sub?.subscription?.currentPeriodEnd
  const trialEnd = sub?.subscription?.trialEnd
  const isTrialing = sub?.subscription?.status === 'trialing'

  return (
    <div className="mx-auto max-w-2xl space-y-8 py-6">
      {/* Toast */}
      {toast && (
        <div className="flex items-center justify-between rounded-lg bg-green-50 px-4 py-3 text-sm text-green-800">
          <div className="flex items-center gap-2">
            <Check className="h-4 w-4" />
            {toast}
          </div>
          <button onClick={() => setToast(null)}>
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {error && (
        <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
          <button className="ml-2 underline" onClick={() => setError(null)}>
            dismiss
          </button>
        </div>
      )}

      {/* ── SportsBooks+ Card ── */}
      <section className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-5 text-white">
          <div className="flex items-center gap-3">
            <Crown className="h-6 w-6 text-yellow-300" />
            <div>
              <h2 className="text-lg font-bold">SportsBooks+</h2>
              <p className="text-sm text-indigo-100">
                {isPlus
                  ? isTrialing
                    ? 'Free trial active'
                    : "You're a Plus member"
                  : 'Unlock premium features'}
              </p>
            </div>
          </div>
        </div>

        <div className="px-6 py-5">
          {isPlus ? (
            <div className="space-y-4">
              {/* Active status */}
              <div className="flex items-center gap-2 text-sm">
                <span className="inline-flex items-center gap-1 rounded-full bg-green-100 px-2.5 py-0.5 font-medium text-green-700">
                  <Check className="h-3 w-3" /> Active
                </span>
                {isTrialing && trialEnd && (
                  <span className="text-gray-500">
                    Trial ends {new Date(trialEnd).toLocaleDateString()}
                  </span>
                )}
                {!isTrialing && periodEnd && (
                  <span className="text-gray-500">
                    {isCanceling ? 'Access until' : 'Renews'}{' '}
                    {new Date(periodEnd).toLocaleDateString()}
                  </span>
                )}
              </div>

              {/* Features list */}
              <ul className="space-y-2 text-sm text-gray-600">
                <PlusFeature>0% service fee on all bookings</PlusFeature>
                <PlusFeature>Priority matchmaking ranking</PlusFeature>
                <PlusFeature>24h cancellation window (vs 48h)</PlusFeature>
                <PlusFeature>Advanced player stats & insights</PlusFeature>
                <PlusFeature>Profile visibility controls</PlusFeature>
                <PlusFeature>SportsBooks+ badge on profile</PlusFeature>
              </ul>

              {/* Cancel / Reactivate */}
              <div className="pt-2">
                {isCanceling ? (
                  <button
                    onClick={handleReactivate}
                    disabled={actionLoading}
                    className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
                  >
                    {actionLoading ? 'Processing...' : 'Reactivate Subscription'}
                  </button>
                ) : (
                  <button
                    onClick={handleCancel}
                    disabled={actionLoading}
                    className="text-sm text-gray-500 underline hover:text-gray-700"
                  >
                    Cancel subscription
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {/* Upsell */}
              <p className="text-sm text-gray-600">
                Upgrade to SportsBooks+ for a better experience. Start with a 7-day free trial.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <PlusFeature>0% service fee on all bookings</PlusFeature>
                <PlusFeature>Priority matchmaking ranking</PlusFeature>
                <PlusFeature>24h cancellation window (vs 48h)</PlusFeature>
                <PlusFeature>Advanced player stats & insights</PlusFeature>
                <PlusFeature>Profile visibility controls</PlusFeature>
                <PlusFeature>SportsBooks+ badge on profile</PlusFeature>
              </ul>
              <button
                onClick={handleUpgrade}
                disabled={actionLoading}
                className="w-full rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-3 text-sm font-bold text-white shadow-sm hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50"
              >
                {actionLoading ? (
                  <span className="flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" /> Starting checkout...
                  </span>
                ) : (
                  'Start 7-day free trial'
                )}
              </button>
            </div>
          )}
        </div>
      </section>

      {/* ── Privacy & Visibility ── */}
      <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
        <div className="mb-4 flex items-center gap-2">
          <Shield className="h-5 w-5 text-gray-700" />
          <h2 className="text-base font-semibold text-gray-900">Profile Visibility</h2>
        </div>
        <p className="mb-4 text-sm text-gray-500">
          Control who can see your profile.{' '}
          {!isPlus && (
            <span className="font-medium text-indigo-600">
              Upgrade to SportsBooks+ to unlock private & friends-only options.
            </span>
          )}
        </p>

        <div className="space-y-3">
          <VisibilityOption
            value="public"
            current={sub?.profileVisibility ?? 'public'}
            icon={<Eye className="h-4 w-4" />}
            label="Public"
            description="Anyone can view your profile"
            disabled={actionLoading}
            onChange={handleVisibilityChange}
          />
          <VisibilityOption
            value="friends_only"
            current={sub?.profileVisibility ?? 'public'}
            icon={<Users className="h-4 w-4" />}
            label="Friends Only"
            description="Only accepted friends can see your full profile"
            disabled={actionLoading || !isPlus}
            locked={!isPlus}
            onChange={handleVisibilityChange}
          />
          <VisibilityOption
            value="private"
            current={sub?.profileVisibility ?? 'public'}
            icon={<EyeOff className="h-4 w-4" />}
            label="Private"
            description="Your profile is hidden from everyone"
            disabled={actionLoading || !isPlus}
            locked={!isPlus}
            onChange={handleVisibilityChange}
          />
        </div>
      </section>
    </div>
  )
}

function PlusFeature({ children }: { children: React.ReactNode }) {
  return (
    <li className="flex items-center gap-2">
      <Check className="h-4 w-4 flex-shrink-0 text-indigo-500" />
      {children}
    </li>
  )
}

function VisibilityOption({
  value,
  current,
  icon,
  label,
  description,
  disabled,
  locked,
  onChange,
}: {
  value: ProfileVisibility
  current: ProfileVisibility
  icon: React.ReactNode
  label: string
  description: string
  disabled: boolean
  locked?: boolean
  onChange: (v: ProfileVisibility) => void
}) {
  const isSelected = current === value
  return (
    <button
      type="button"
      onClick={() => onChange(value)}
      disabled={disabled}
      className={`flex w-full items-center gap-3 rounded-xl border px-4 py-3 text-left transition ${
        isSelected
          ? 'border-indigo-300 bg-indigo-50'
          : 'border-gray-200 bg-white hover:border-gray-300'
      } ${disabled && !isSelected ? 'cursor-not-allowed opacity-50' : ''}`}
    >
      <div
        className={`flex h-8 w-8 items-center justify-center rounded-lg ${
          isSelected ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-500'
        }`}
      >
        {icon}
      </div>
      <div className="flex-1">
        <div className="flex items-center gap-2">
          <span className={`text-sm font-medium ${isSelected ? 'text-indigo-900' : 'text-gray-900'}`}>
            {label}
          </span>
          {locked && (
            <span className="rounded bg-indigo-100 px-1.5 py-0.5 text-xs font-medium text-indigo-600">
              Plus
            </span>
          )}
        </div>
        <span className="text-xs text-gray-500">{description}</span>
      </div>
      {isSelected && (
        <div className="flex h-5 w-5 items-center justify-center rounded-full bg-indigo-600">
          <Check className="h-3 w-3 text-white" />
        </div>
      )}
    </button>
  )
}
