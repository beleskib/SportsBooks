// ============================================================
// Partner approval emails — sent when a player requests a booking.
//
// Flow:
//   1. Player creates a booking → status = 'pending'
//   2. We email the partner immediately with a link to the dashboard.
//      The link requires Firebase login (no magic-link tokens) so the
//      action is 100% authenticated.
//   3. If still 'pending' at +2h and +6h, send a reminder email.
//      The reminder check runs as a periodic job in
//      ./bookingApprovalReminder.service.
//
// We DO NOT auto-approve. Per product owner: "Login required so it is
// 100% accurate" — a stale request is better than a wrong one.
// ============================================================

import { sendEmail } from './email.service';
import { BookingRow } from '../repositories/booking.repository';
import * as userRepo from '../repositories/user.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';

const FRONTEND_URL = process.env.FRONTEND_BASE_URL ?? 'http://localhost:5173';

/** Reminder cadence (hours from booking creation). */
export const REMINDER_OFFSETS_HOURS = [2, 6] as const;

interface PartnerEmailContext {
  partnerEmail: string;
  partnerName: string;
  entityName: string;
  address: string;
  approvalUrl: string;
  slotWhen: string;
  playerName: string;
  totalPrice: number;
}

function formatSlotWhen(date: string, startTime: string, endTime: string): string {
  if (!date) return 'Date TBC';
  const [y, m, d] = date.split('-').map(Number);
  const dt = new Date(Date.UTC(y, m - 1, d));
  const datePart = dt.toLocaleDateString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short', timeZone: 'UTC',
  });
  return `${datePart} · ${startTime?.substring(0, 5)}–${endTime?.substring(0, 5)}`;
}

async function buildContext(booking: BookingRow): Promise<PartnerEmailContext | null> {
  let entityName = '';
  let address = '';
  let partnerEmail: string | null = null;
  let partnerName: string | null = null;

  if (booking.venueId) {
    const venue = await venueRepo.findById(booking.venueId);
    if (venue) {
      entityName = venue.name;
      address = venue.address ?? '';
      const owner = await userRepo.findById(venue.ownerId);
      partnerEmail = owner?.email ?? null;
      partnerName = owner?.displayName ?? null;
    }
  } else if (booking.coachId) {
    const coach = await coachRepo.findById(booking.coachId);
    if (coach) {
      entityName = coach.name;
      const owner = await userRepo.findById(coach.userId);
      partnerEmail = owner?.email ?? null;
      partnerName = owner?.displayName ?? null;
    }
  }

  if (!partnerEmail) return null;

  const slot = booking.timeSlot;
  const slotWhen = slot ? formatSlotWhen(slot.slotDate, slot.startTime, slot.endTime) : 'Date TBC';
  // Login-gated dashboard URL — frontend redirects through Firebase auth if needed.
  const approvalUrl = `${FRONTEND_URL}/partner/reservations/${booking.id}`;

  return {
    partnerEmail,
    partnerName: partnerName ?? 'there',
    entityName,
    address,
    approvalUrl,
    slotWhen,
    playerName: booking.playerName ?? booking.playerEmail ?? 'A player',
    totalPrice: booking.totalPrice,
  };
}

function renderHtml(ctx: PartnerEmailContext, isReminder: boolean, hoursElapsed?: number): string {
  const heading = isReminder ? 'Still waiting on your approval' : 'New booking request';
  const intro = isReminder
    ? `This request has been sitting for ${hoursElapsed}h. Players see "pending" until you respond.`
    : `<strong>${ctx.playerName}</strong> wants to book <strong>${ctx.entityName}</strong>.`;

  return `<!DOCTYPE html>
<html><body style="font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;background:#F4F4EF;margin:0;padding:24px;color:#0F1A2A;">
  <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;border:1px solid #E5E7EB;">
    <div style="background:#0F1A2A;color:#E8B931;padding:20px 28px;font-weight:700;font-size:18px;">SportsBooks</div>
    <div style="padding:28px;">
      <h1 style="margin:0 0 8px 0;font-size:22px;">${heading}</h1>
      <p style="margin:0 0 18px 0;color:#4B5563;font-size:15px;line-height:1.5;">Hi ${ctx.partnerName}, ${intro}</p>

      <div style="background:#F9FAFB;border-radius:10px;padding:16px 18px;margin-bottom:18px;">
        <div style="font-size:16px;font-weight:600;margin-bottom:4px;">${ctx.entityName}</div>
        <div style="color:#4B5563;font-size:14px;">${ctx.slotWhen}</div>
        ${ctx.address ? `<div style="color:#6B7280;font-size:13px;margin-top:4px;">${ctx.address}</div>` : ''}
        <div style="color:#4B5563;font-size:14px;margin-top:8px;">Requested by <strong>${ctx.playerName}</strong></div>
        <div style="color:#0F1A2A;font-size:14px;margin-top:4px;font-weight:600;">${ctx.totalPrice.toFixed(2)} ден</div>
      </div>

      <a href="${ctx.approvalUrl}" style="display:inline-block;background:#0F1A2A;color:#E8B931;padding:12px 22px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">Open in dashboard</a>

      <p style="margin:20px 0 0 0;color:#6B7280;font-size:12px;line-height:1.5;">
        You'll be asked to sign in before you can approve or decline. We never act on your
        behalf from a single email click — protects you and the player.
      </p>
    </div>
  </div>
</body></html>`;
}

/** Initial partner email — sent immediately when a player requests a booking. */
export async function sendPartnerBookingRequestEmail(bookingId: number): Promise<void> {
  try {
    const { findById: findBookingById } = await import('../repositories/booking.repository');
    const booking = await findBookingById(bookingId);
    if (!booking) return;
    const ctx = await buildContext(booking);
    if (!ctx) return;

    await sendEmail({
      to: ctx.partnerEmail,
      subject: `New booking request · ${ctx.entityName} · ${ctx.slotWhen}`,
      html: renderHtml(ctx, false),
    });
  } catch (err) {
    console.error('[partnerApproval] sendPartnerBookingRequestEmail failed:', err);
  }
}

/**
 * Reminder email — sent by the periodic job (./bookingApprovalReminder)
 * when a request has been pending for `hoursElapsed` (one of REMINDER_OFFSETS_HOURS).
 */
export async function sendPartnerBookingReminderEmail(
  bookingId: number,
  hoursElapsed: number
): Promise<void> {
  try {
    const { findById: findBookingById } = await import('../repositories/booking.repository');
    const booking = await findBookingById(bookingId);
    if (!booking || booking.status !== 'pending') return; // bail if already resolved
    const ctx = await buildContext(booking);
    if (!ctx) return;

    await sendEmail({
      to: ctx.partnerEmail,
      subject: `Reminder · pending booking for ${ctx.entityName} (${hoursElapsed}h)`,
      html: renderHtml(ctx, true, hoursElapsed),
    });
  } catch (err) {
    console.error('[partnerApproval] sendPartnerBookingReminderEmail failed:', err);
  }
}
