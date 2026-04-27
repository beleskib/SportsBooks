// ============================================================
// Periodic job: send partner approval reminders for stale bookings.
//
// Runs every 5 minutes. For each pending booking that has been waiting
// 2h or 6h and has NOT yet had the corresponding reminder sent, we:
//   1. Atomically claim the booking by setting partner_reminded_Nh_at = NOW()
//      via UPDATE ... RETURNING id (idempotency without distributed locks).
//   2. Send the reminder email.
// If the email fails, the timestamp is already set — we don't retry that
// specific reminder. The +6h reminder still has a chance to fire.
// ============================================================

import { query } from '../config/database';
import { sendPartnerBookingReminderEmail } from './partnerApproval.service';

const POLL_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
let interval: NodeJS.Timeout | null = null;

interface ReminderRow { id: string }

/**
 * Run one pass of the reminder check. Exposed for testing — the cron just
 * calls this on a timer.
 */
export async function runReminderPass(): Promise<{ sent2h: number; sent6h: number }> {
  let sent2h = 0;
  let sent6h = 0;

  // 2h reminder: claim rows that are 2-6h old (so we don't double up with 6h)
  const claim2h = await query(
    `UPDATE bookings
     SET partner_reminded_2h_at = NOW()
     WHERE status = 'pending'
       AND partner_reminded_2h_at IS NULL
       AND created_at <= NOW() - INTERVAL '2 hours'
       AND created_at >  NOW() - INTERVAL '6 hours'
     RETURNING id`
  );
  for (const row of claim2h.rows as ReminderRow[]) {
    await sendPartnerBookingReminderEmail(Number(row.id), 2);
    sent2h++;
  }

  // 6h reminder: claim rows that are 6h+ old
  const claim6h = await query(
    `UPDATE bookings
     SET partner_reminded_6h_at = NOW()
     WHERE status = 'pending'
       AND partner_reminded_6h_at IS NULL
       AND created_at <= NOW() - INTERVAL '6 hours'
     RETURNING id`
  );
  for (const row of claim6h.rows as ReminderRow[]) {
    await sendPartnerBookingReminderEmail(Number(row.id), 6);
    sent6h++;
  }

  return { sent2h, sent6h };
}

/** Start the periodic timer. Idempotent — multiple calls are no-ops. */
export function startReminderJob(): void {
  if (interval) return;
  // Skip during test runs to avoid noisy logs / DB hits.
  if (process.env.NODE_ENV === 'test') return;

  // Best-effort: if the very first pass fails, log and keep going.
  runReminderPass()
    .then((r) => {
      if (r.sent2h || r.sent6h) {
        console.log(`[approvalReminder] startup pass: 2h=${r.sent2h} 6h=${r.sent6h}`);
      }
    })
    .catch((e) => console.error('[approvalReminder] startup pass failed:', e));

  interval = setInterval(() => {
    runReminderPass()
      .then((r) => {
        if (r.sent2h || r.sent6h) {
          console.log(`[approvalReminder] tick: 2h=${r.sent2h} 6h=${r.sent6h}`);
        }
      })
      .catch((e) => console.error('[approvalReminder] tick failed:', e));
  }, POLL_INTERVAL_MS);

  console.log(`[approvalReminder] cron started (every ${POLL_INTERVAL_MS / 1000}s)`);
}

/** Stop the timer — used by tests / graceful shutdown. */
export function stopReminderJob(): void {
  if (interval) {
    clearInterval(interval);
    interval = null;
  }
}
