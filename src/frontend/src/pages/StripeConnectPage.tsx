import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CreditCard,
  ExternalLink,
  CheckCircle2,
  Clock,
  AlertCircle,
  RefreshCw,
  Banknote,
} from 'lucide-react'
import { stripeConnectApi, type StripeAccountStatusResponse } from '@/api/stripeConnect'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

type OnboardingStep = 'loading' | 'not_started' | 'pending' | 'complete' | 'error'

export function StripeConnectPage() {
  const [searchParams] = useSearchParams()
  const [step, setStep] = useState<OnboardingStep>('loading')
  const [status, setStatus] = useState<StripeAccountStatusResponse | null>(null)
  const [onboardingUrl, setOnboardingUrl] = useState<string | null>(null)
  const [isOnboarding, setIsOnboarding] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchStatus = useCallback(async () => {
    try {
      setError(null)
      const res = await stripeConnectApi.getStatus()
      setStatus(res.data)

      if (res.data.onboardingStatus === 'complete' && res.data.payoutsEnabled) {
        setStep('complete')
      } else if (res.data.onboardingStatus === 'pending') {
        setStep('pending')
      } else {
        setStep('not_started')
      }
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message || 'Failed to load status'
      setError(msg)
      setStep('error')
    }
  }, [])

  useEffect(() => {
    // If returning from Stripe onboarding, refresh status
    const stripeStatus = searchParams.get('status')
    if (stripeStatus === 'return' || stripeStatus === 'refresh') {
      fetchStatus()
    } else {
      fetchStatus()
    }
  }, [searchParams, fetchStatus])

  const handleStartOnboarding = async () => {
    try {
      setIsOnboarding(true)
      setError(null)
      const res = await stripeConnectApi.onboard()
      setOnboardingUrl(res.data.onboardingUrl)
      // Redirect to Stripe
      window.location.href = res.data.onboardingUrl
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message || 'Failed to start onboarding'
      setError(msg)
      setIsOnboarding(false)
    }
  }

  const handleOpenDashboard = async () => {
    try {
      setError(null)
      const res = await stripeConnectApi.getDashboardLink()
      window.open(res.data.dashboardUrl, '_blank')
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message || 'Failed to open dashboard'
      setError(msg)
    }
  }

  if (step === 'loading') return <LoadingSpinner />

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="mb-2 text-2xl font-bold text-gray-900">Payment Setup</h2>
      <p className="mb-8 text-sm text-gray-500">
        Connect your Stripe account to receive payouts from bookings. We use Stripe
        in <span className="font-medium text-orange-600">test mode</span> — no real money is involved.
      </p>

      {error && (
        <div className="mb-6 flex items-start gap-3 rounded-lg bg-red-50 p-4">
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" />
          <div>
            <p className="text-sm font-medium text-red-800">Something went wrong</p>
            <p className="mt-1 text-sm text-red-700">{error}</p>
          </div>
        </div>
      )}

      {/* How It Works */}
      <div className="mb-8 rounded-lg bg-white p-6 shadow">
        <h3 className="mb-4 text-lg font-semibold text-gray-900">How Payments Work</h3>
        <div className="space-y-4">
          <FlowStep
            number={1}
            title="Player books a time slot"
            description="The player pays through the app via Stripe."
          />
          <FlowStep
            number={2}
            title="Platform collects the payment"
            description="SportsBooks securely holds the funds."
          />
          <FlowStep
            number={3}
            title="You receive your payout"
            description="We deduct a 10% platform fee and transfer the rest to your bank account automatically."
          />
        </div>
        <div className="mt-4 rounded-md bg-blue-50 px-4 py-3">
          <p className="text-sm text-blue-800">
            <strong>Example:</strong> A player books for $50 → Platform fee: $5 (10%) → You receive: $45
          </p>
        </div>
      </div>

      {/* Status Card */}
      {step === 'not_started' && (
        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center gap-3 mb-4">
            <div className="rounded-lg bg-purple-100 p-2">
              <CreditCard className="h-6 w-6 text-purple-600" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">Connect Your Stripe Account</h3>
              <p className="text-sm text-gray-500">Set up payouts in just a few minutes</p>
            </div>
          </div>
          <p className="mb-6 text-sm text-gray-600">
            You'll be redirected to Stripe to securely enter your banking details.
            Since we're in <strong>test mode</strong>, you can use test data — no real
            bank account is needed.
          </p>
          <div className="mb-4 rounded-md bg-amber-50 px-4 py-3">
            <p className="text-sm text-amber-800">
              <strong>Test mode tip:</strong> Use phone number <code className="rounded bg-amber-100 px-1">000 000 0000</code>,
              and verification code <code className="rounded bg-amber-100 px-1">000000</code> during onboarding.
              For bank routing use <code className="rounded bg-amber-100 px-1">110000000</code> and account <code className="rounded bg-amber-100 px-1">000123456789</code>.
            </p>
          </div>
          <button
            onClick={handleStartOnboarding}
            disabled={isOnboarding}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-purple-600 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-purple-700 disabled:opacity-50"
          >
            {isOnboarding ? (
              <>
                <RefreshCw className="h-4 w-4 animate-spin" />
                Redirecting to Stripe...
              </>
            ) : (
              <>
                <Banknote className="h-4 w-4" />
                Set Up Payouts with Stripe
              </>
            )}
          </button>
        </div>
      )}

      {step === 'pending' && (
        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center gap-3 mb-4">
            <div className="rounded-lg bg-yellow-100 p-2">
              <Clock className="h-6 w-6 text-yellow-600" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">Onboarding In Progress</h3>
              <p className="text-sm text-gray-500">Your Stripe account setup is not yet complete</p>
            </div>
          </div>
          <p className="mb-6 text-sm text-gray-600">
            It looks like you started the onboarding process but haven't finished it yet.
            Click below to continue where you left off.
          </p>
          <div className="flex gap-3">
            <button
              onClick={handleStartOnboarding}
              disabled={isOnboarding}
              className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-yellow-600 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-yellow-700 disabled:opacity-50"
            >
              {isOnboarding ? (
                <>
                  <RefreshCw className="h-4 w-4 animate-spin" />
                  Redirecting...
                </>
              ) : (
                <>
                  <ExternalLink className="h-4 w-4" />
                  Continue Onboarding
                </>
              )}
            </button>
            <button
              onClick={fetchStatus}
              className="rounded-lg border border-gray-300 px-4 py-3 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50"
            >
              <RefreshCw className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {step === 'complete' && (
        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center gap-3 mb-4">
            <div className="rounded-lg bg-green-100 p-2">
              <CheckCircle2 className="h-6 w-6 text-green-600" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">Payouts Active</h3>
              <p className="text-sm text-gray-500">Your Stripe account is fully set up</p>
            </div>
          </div>
          <div className="mb-6 space-y-2">
            <StatusRow label="Stripe Account" value={status?.stripeAccountId ?? '—'} />
            <StatusRow
              label="Payouts"
              value={status?.payoutsEnabled ? 'Enabled' : 'Disabled'}
              success={status?.payoutsEnabled}
            />
            <StatusRow label="Platform Fee" value="10% per booking" />
          </div>
          <button
            onClick={handleOpenDashboard}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-gray-900 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-gray-800"
          >
            <ExternalLink className="h-4 w-4" />
            Open Stripe Dashboard
          </button>
          <p className="mt-3 text-center text-xs text-gray-400">
            View your payouts, transaction history, and banking details on Stripe.
          </p>
        </div>
      )}

      {step === 'error' && (
        <div className="rounded-lg bg-white p-6 shadow">
          <div className="text-center">
            <AlertCircle className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-4 font-semibold text-gray-900">Unable to Load Status</h3>
            <p className="mt-2 text-sm text-gray-500">
              There was a problem loading your payment setup status. Please try again.
            </p>
            <button
              onClick={fetchStatus}
              className="mt-4 inline-flex items-center gap-2 rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-800"
            >
              <RefreshCw className="h-4 w-4" />
              Retry
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function FlowStep({ number, title, description }: { number: number; title: string; description: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-gray-900 text-xs font-bold text-white">
        {number}
      </div>
      <div>
        <p className="text-sm font-medium text-gray-900">{title}</p>
        <p className="text-sm text-gray-500">{description}</p>
      </div>
    </div>
  )
}

function StatusRow({ label, value, success }: { label: string; value: string; success?: boolean }) {
  return (
    <div className="flex items-center justify-between rounded-md bg-gray-50 px-3 py-2">
      <span className="text-sm text-gray-500">{label}</span>
      <span className={`text-sm font-medium ${success === true ? 'text-green-600' : success === false ? 'text-red-600' : 'text-gray-900'}`}>
        {value}
      </span>
    </div>
  )
}
