// ============================================================
// Listing approval emails — venue + coach approval lifecycle.
//
// Three triggers:
//   1. Partner submits a venue/coach   → notify admin reviewer
//   2. Admin approves the listing       → notify partner ("you're live")
//   3. Admin rejects the listing        → notify partner with reason
//
// All sends are fire-and-forget: failures are logged but never block the
// HTTP response. Falls through to email.service stub mode when RESEND_API_KEY
// is unset, so dev environments don't need a real provider.
// ============================================================

import { sendEmail } from './email.service';
import { query } from '../config/database';

const FRONTEND_URL = process.env.FRONTEND_BASE_URL ?? 'http://localhost:5173';
const ADMIN_REVIEW_EMAIL = process.env.ADMIN_REVIEW_EMAIL ?? 'bojanbeleski@gmail.com';

export type ListingType = 'venue' | 'coach';

interface ListingMeta {
  type: ListingType;
  id: number;
  name: string;
  sportType: string;
  city: string | null;
  ownerEmail: string;
  ownerDisplayName: string | null;
}

async function loadListingMeta(type: ListingType, id: number): Promise<ListingMeta | null> {
  const sql = type === 'venue'
    ? `SELECT v.id, v.name, v.sport_type, v.city,
              u.email AS owner_email, u.display_name AS owner_display_name
       FROM venues v INNER JOIN users u ON u.id = v.owner_id
       WHERE v.id = $1`
    : `SELECT c.id, c.name, c.sport_type, c.city,
              u.email AS owner_email, u.display_name AS owner_display_name
       FROM coaches c INNER JOIN users u ON u.id = c.user_id
       WHERE c.id = $1`;
  const r = await query(sql, [id]);
  if (r.rows.length === 0) return null;
  const row = r.rows[0];
  return {
    type,
    id: Number(row.id),
    name: row.name,
    sportType: row.sport_type,
    city: row.city,
    ownerEmail: row.owner_email,
    ownerDisplayName: row.owner_display_name,
  };
}

// Brand-consistent shell — Navy900 + USOpenGold (matches partnerApproval.service).
function shell(heading: string, bodyHtml: string, ctaLabel?: string, ctaUrl?: string): string {
  const cta = ctaLabel && ctaUrl
    ? `<a href="${ctaUrl}" style="display:inline-block;background:#0F1A2A;color:#E8B931;padding:12px 22px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">${ctaLabel}</a>`
    : '';
  return `<!DOCTYPE html>
<html><body style="font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;background:#F4F4EF;margin:0;padding:24px;color:#0F1A2A;">
  <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;border:1px solid #E5E7EB;">
    <div style="background:#0F1A2A;color:#E8B931;padding:20px 28px;font-weight:700;font-size:18px;">SportsBooks</div>
    <div style="padding:28px;">
      <h1 style="margin:0 0 16px 0;font-size:22px;">${heading}</h1>
      ${bodyHtml}
      ${cta ? `<div style="margin-top:20px;">${cta}</div>` : ''}
    </div>
  </div>
</body></html>`;
}

/**
 * Trigger 1 — partner just submitted a listing. Email the admin reviewer
 * with a link straight to the review page.
 */
export async function notifyAdminListingSubmitted(type: ListingType, id: number): Promise<void> {
  try {
    const meta = await loadListingMeta(type, id);
    if (!meta) return;

    const reviewUrl = `${FRONTEND_URL}/admin/listings/${type}/${meta.id}`;
    const ownerLabel = meta.ownerDisplayName
      ? `${meta.ownerDisplayName} (${meta.ownerEmail})`
      : meta.ownerEmail;

    await sendEmail({
      to: ADMIN_REVIEW_EMAIL,
      subject: `New ${type} pending review · ${meta.name}`,
      html: shell(
        `New ${type} pending your review`,
        `<p style="margin:0 0 14px 0;color:#4B5563;font-size:15px;line-height:1.5;">
          A partner just submitted a ${type} for review. Check the details and approve
          or send it back with feedback.
        </p>
        <div style="background:#F9FAFB;border-radius:10px;padding:16px 18px;margin:16px 0;">
          <div style="font-size:16px;font-weight:600;margin-bottom:4px;">${meta.name}</div>
          <div style="color:#4B5563;font-size:14px;">${meta.sportType}${meta.city ? ` · ${meta.city}` : ''}</div>
          <div style="color:#6B7280;font-size:13px;margin-top:6px;">Submitted by ${ownerLabel}</div>
        </div>`,
        'Open in dashboard',
        reviewUrl
      ),
    });
  } catch (err) {
    console.error('[listingApproval] notifyAdminListingSubmitted failed:', err);
  }
}

/**
 * Trigger 2 — admin approved the listing. Email the partner.
 */
export async function notifyPartnerListingApproved(type: ListingType, id: number): Promise<void> {
  try {
    const meta = await loadListingMeta(type, id);
    if (!meta) return;

    const dashboardUrl = `${FRONTEND_URL}/dashboard`;
    const greeting = meta.ownerDisplayName ? `Hi ${meta.ownerDisplayName},` : 'Hi,';
    const noun = type === 'venue' ? 'venue' : 'coach profile';

    await sendEmail({
      to: meta.ownerEmail,
      subject: `Your ${noun} "${meta.name}" is live`,
      html: shell(
        `${meta.name} is live`,
        `<p style="margin:0 0 14px 0;color:#4B5563;font-size:15px;line-height:1.5;">${greeting}</p>
        <p style="margin:0 0 14px 0;color:#0F1A2A;font-size:15px;line-height:1.5;">
          Your ${noun} <strong>${meta.name}</strong> just passed review and is now visible to
          players. You can manage time slots and bookings from your dashboard.
        </p>`,
        'Open dashboard',
        dashboardUrl
      ),
    });
  } catch (err) {
    console.error('[listingApproval] notifyPartnerListingApproved failed:', err);
  }
}

/**
 * Trigger 3 — admin rejected the listing. Email the partner with reason
 * so they know what to fix.
 */
export async function notifyPartnerListingRejected(
  type: ListingType,
  id: number,
  reason: string
): Promise<void> {
  try {
    const meta = await loadListingMeta(type, id);
    if (!meta) return;

    const dashboardUrl = `${FRONTEND_URL}/dashboard`;
    const greeting = meta.ownerDisplayName ? `Hi ${meta.ownerDisplayName},` : 'Hi,';
    const noun = type === 'venue' ? 'venue' : 'coach profile';
    const safeReason = reason
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br/>');

    await sendEmail({
      to: meta.ownerEmail,
      subject: `${meta.name} needs changes before going live`,
      html: shell(
        `Changes needed on ${meta.name}`,
        `<p style="margin:0 0 14px 0;color:#4B5563;font-size:15px;line-height:1.5;">${greeting}</p>
        <p style="margin:0 0 14px 0;color:#0F1A2A;font-size:15px;line-height:1.5;">
          We can't publish your ${noun} <strong>${meta.name}</strong> just yet. Reviewer notes:
        </p>
        <div style="background:#FEF3C7;border-left:4px solid #E8B931;border-radius:6px;padding:12px 14px;color:#0F1A2A;font-size:14px;line-height:1.5;margin:0 0 16px 0;">
          ${safeReason}
        </div>
        <p style="margin:0;color:#4B5563;font-size:14px;line-height:1.5;">
          Update the listing and re-submit — we'll review again.
        </p>`,
        'Open dashboard',
        dashboardUrl
      ),
    });
  } catch (err) {
    console.error('[listingApproval] notifyPartnerListingRejected failed:', err);
  }
}
