// ============================================================
// Booking confirmation email — receipt + .ics calendar attachment.
//
// Design rules (from product owner):
//  - Player and partner receive the same receipt template.
//  - Platform fee = 5% of slot price (charged to player).
//  - Stripe processing fee (~2.9% + €0.30) is absorbed by SportsBooks
//    — NOT shown on the receipt.
//  - Cancellation policy: free up to 12h before slot start; no refund inside.
//  - Two CTAs: "View booking" deep link + .ics attachment for calendar add.
// ============================================================

import ical from 'ical-generator';
import { sendEmail } from './email.service';
import { BookingRow } from '../repositories/booking.repository';
import * as userRepo from '../repositories/user.repository';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';

const FRONTEND_URL = process.env.FRONTEND_BASE_URL ?? 'http://localhost:5173';
const PLATFORM_FEE_RATE = 0.05; // 5%
const CANCELLATION_WINDOW_HOURS = 12;
const CURRENCY_LABEL = 'ден'; // matches in-app currency display

interface ReceiptLineItems {
  slotPrice: number;
  platformFee: number;
  total: number;
  partnerPayout: number;
}

function calcLineItems(slotPrice: number): ReceiptLineItems {
  const platformFee = round2(slotPrice * PLATFORM_FEE_RATE);
  return {
    slotPrice: round2(slotPrice),
    platformFee,
    total: round2(slotPrice + platformFee),
    // Partner sees the gross slot price; platform fee is what we keep.
    partnerPayout: round2(slotPrice),
  };
}

const round2 = (n: number) => Math.round(n * 100) / 100;

function formatMoney(amount: number): string {
  return `${amount.toFixed(2)} ${CURRENCY_LABEL}`;
}

function formatSlotDateTime(date: string, startTime: string, endTime: string): string {
  // date: 'YYYY-MM-DD', times: 'HH:MM:SS' or 'HH:MM'
  if (!date) return 'Date TBC';
  const [y, m, d] = date.split('-').map(Number);
  const dt = new Date(Date.UTC(y, m - 1, d));
  const datePart = dt.toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC',
  });
  return `${datePart} · ${startTime?.substring(0, 5) ?? ''}–${endTime?.substring(0, 5) ?? ''}`;
}

function buildIcs(booking: BookingRow, entityName: string, address: string | undefined): string {
  const slot = booking.timeSlot;
  if (!slot?.slotDate || !slot.startTime || !slot.endTime) return '';

  const cal = ical({ name: 'SportsBooks' });
  const [y, m, d] = slot.slotDate.split('-').map(Number);
  const [sh, sm] = slot.startTime.split(':').map(Number);
  const [eh, em] = slot.endTime.split(':').map(Number);

  cal.createEvent({
    start: new Date(y, m - 1, d, sh, sm),
    end: new Date(y, m - 1, d, eh, em),
    summary: `${entityName} — SportsBooks`,
    description: `Booking #${booking.id}. View at ${FRONTEND_URL}/bookings/${booking.id}`,
    location: address ?? entityName,
    url: `${FRONTEND_URL}/bookings/${booking.id}`,
  });
  return cal.toString();
}

function renderHtml(opts: {
  recipientLabel: 'player' | 'partner';
  recipientName: string;
  booking: BookingRow;
  entityName: string;
  address: string;
  lineItems: ReceiptLineItems;
  slotWhen: string;
  bookingUrl: string;
}): string {
  const { recipientLabel, recipientName, booking, entityName, address, lineItems, slotWhen, bookingUrl } = opts;
  const heading = recipientLabel === 'player' ? 'Booking confirmed' : 'New booking confirmed';
  const intro = recipientLabel === 'player'
    ? `Your booking at <strong>${entityName}</strong> is locked in.`
    : `<strong>${booking.playerName ?? booking.playerEmail ?? 'A player'}</strong> just booked <strong>${entityName}</strong>.`;
  const ctaLabel = recipientLabel === 'player' ? 'View booking' : 'Open partner dashboard';
  const ctaUrl = recipientLabel === 'player'
    ? bookingUrl
    : `${FRONTEND_URL}/partner/reservations/${booking.id}`;

  return `<!DOCTYPE html>
<html><body style="font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;background:#F4F4EF;margin:0;padding:24px;color:#0F1A2A;">
  <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;border:1px solid #E5E7EB;">
    <div style="background:#0F1A2A;color:#E8B931;padding:20px 28px;font-weight:700;font-size:18px;letter-spacing:0.3px;">
      SportsBooks
    </div>
    <div style="padding:28px;">
      <h1 style="margin:0 0 8px 0;font-size:24px;line-height:1.2;">${heading}</h1>
      <p style="margin:0 0 20px 0;color:#4B5563;font-size:15px;line-height:1.5;">Hi ${recipientName}, ${intro}</p>

      <div style="background:#F9FAFB;border-radius:10px;padding:18px 20px;margin-bottom:20px;">
        <div style="font-size:18px;font-weight:600;margin-bottom:6px;">${entityName}</div>
        <div style="color:#4B5563;font-size:14px;margin-bottom:4px;">${slotWhen}</div>
        ${address ? `<div style="color:#6B7280;font-size:14px;">${address}</div>` : ''}
      </div>

      <table style="width:100%;border-collapse:collapse;font-size:14px;margin-bottom:18px;">
        <tr><td style="padding:6px 0;color:#4B5563;">Slot price</td>
            <td style="padding:6px 0;text-align:right;">${formatMoney(lineItems.slotPrice)}</td></tr>
        <tr><td style="padding:6px 0;color:#4B5563;">Platform fee (5%)</td>
            <td style="padding:6px 0;text-align:right;">${formatMoney(lineItems.platformFee)}</td></tr>
        <tr><td colspan="2" style="border-top:1px solid #E5E7EB;height:1px;padding:0;"></td></tr>
        <tr><td style="padding:10px 0;font-weight:700;">Total ${recipientLabel === 'player' ? 'charged' : ''}</td>
            <td style="padding:10px 0;text-align:right;font-weight:700;">${formatMoney(lineItems.total)}</td></tr>
        ${recipientLabel === 'partner' ? `
        <tr><td style="padding:6px 0;color:#4B5563;">Your payout</td>
            <td style="padding:6px 0;text-align:right;color:#0F1A2A;font-weight:600;">${formatMoney(lineItems.partnerPayout)}</td></tr>
        ` : ''}
      </table>

      <a href="${ctaUrl}" style="display:inline-block;background:#0F1A2A;color:#E8B931;padding:12px 22px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">${ctaLabel}</a>

      <p style="margin:24px 0 4px 0;color:#6B7280;font-size:12px;line-height:1.5;">
        <strong>Cancellation policy:</strong> free cancellation up to ${CANCELLATION_WINDOW_HOURS}h before
        the slot starts. Inside that window, the booking is non-refundable.
      </p>
      <p style="margin:0;color:#9CA3AF;font-size:12px;">
        Booking #${booking.id} · ${recipientLabel === 'player' ? 'Receipt for your records.' : 'Forwarded to you because you own this listing.'}
      </p>
    </div>
  </div>
</body></html>`;
}

/**
 * Send the booking confirmation receipt to both player and partner.
 * Fire-and-forget — failures are logged but never bubble up to the caller.
 */
export async function sendBookingConfirmedEmails(bookingId: number): Promise<void> {
  try {
    // Re-fetch via repo to get joined venue/coach/player rows
    const { findById: findBookingById } = await import('../repositories/booking.repository');
    const booking = await findBookingById(bookingId);
    if (!booking) {
      console.warn(`[bookingEmail] booking ${bookingId} not found, skipping receipt`);
      return;
    }

    // Resolve entity (venue or coach) for the human-readable name + address + partner contact
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

    const slot = booking.timeSlot;
    const slotWhen = slot
      ? formatSlotDateTime(slot.slotDate, slot.startTime, slot.endTime)
      : 'Date TBC';
    const lineItems = calcLineItems(booking.totalPrice);
    const bookingUrl = `${FRONTEND_URL}/bookings/${booking.id}`;
    const ics = buildIcs(booking, entityName, address);
    const attachments = ics ? [{
      filename: 'sportsbooks.ics',
      content: Buffer.from(ics, 'utf-8'),
      contentType: 'text/calendar',
    }] : undefined;

    // Player receipt
    const playerEmail = booking.playerEmail;
    if (playerEmail) {
      await sendEmail({
        to: playerEmail,
        subject: `Booking confirmed · ${entityName}`,
        html: renderHtml({
          recipientLabel: 'player',
          recipientName: booking.playerName ?? 'there',
          booking,
          entityName,
          address,
          lineItems,
          slotWhen,
          bookingUrl,
        }),
        attachments,
      });
    } else {
      console.warn(`[bookingEmail] booking ${bookingId} has no player email`);
    }

    // Partner receipt (same template, partner framing)
    if (partnerEmail) {
      await sendEmail({
        to: partnerEmail,
        subject: `New booking · ${entityName} · ${slotWhen}`,
        html: renderHtml({
          recipientLabel: 'partner',
          recipientName: partnerName ?? 'there',
          booking,
          entityName,
          address,
          lineItems,
          slotWhen,
          bookingUrl,
        }),
        attachments,
      });
    } else {
      console.warn(`[bookingEmail] booking ${bookingId} has no partner email`);
    }
  } catch (err) {
    console.error('[bookingEmail] sendBookingConfirmedEmails failed:', err);
  }
}
