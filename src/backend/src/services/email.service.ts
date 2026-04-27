// ============================================================
// Email service — thin wrapper over Resend with a stub fallback.
//
// Modes:
//   - real: RESEND_API_KEY is set → emails go out via Resend
//   - stub: missing key → log payload to stdout + write to ./tmp/emails/<ts>.eml
//           so dev environments don't need a real provider.
//
// All callers go through `sendEmail()` so swapping providers later
// (Postmark, SendGrid, SES) is a one-file change.
// ============================================================

import { Resend } from 'resend';
import * as fs from 'fs';
import * as path from 'path';

const RESEND_KEY = process.env.RESEND_API_KEY ?? '';
const FROM_ADDRESS = process.env.EMAIL_FROM ?? 'SportsBooks <onboarding@resend.dev>';
const STUB_DIR = path.resolve(process.cwd(), 'tmp', 'emails');

const resend: Resend | null = RESEND_KEY ? new Resend(RESEND_KEY) : null;

export interface EmailAttachment {
  filename: string;
  content: string | Buffer; // base64 string or Buffer
  contentType?: string;
}

export interface SendEmailParams {
  to: string | string[];
  subject: string;
  html: string;
  text?: string; // optional plain-text fallback
  attachments?: EmailAttachment[];
  replyTo?: string;
}

export interface SendEmailResult {
  ok: boolean;
  id?: string;
  mode: 'resend' | 'stub';
  error?: string;
}

/**
 * Send an email. Always returns a result — never throws — so callers can
 * fire-and-forget without wrapping in try/catch. Failures are logged.
 */
export async function sendEmail(params: SendEmailParams): Promise<SendEmailResult> {
  if (!resend) {
    return stubSend(params);
  }

  try {
    const recipients = Array.isArray(params.to) ? params.to : [params.to];
    const result = await resend.emails.send({
      from: FROM_ADDRESS,
      to: recipients,
      subject: params.subject,
      html: params.html,
      text: params.text,
      replyTo: params.replyTo,
      attachments: params.attachments?.map((a) => ({
        filename: a.filename,
        content: typeof a.content === 'string' ? a.content : a.content.toString('base64'),
      })),
    });

    if (result.error) {
      console.error('[email] Resend rejected send:', result.error);
      return { ok: false, mode: 'resend', error: result.error.message };
    }
    return { ok: true, mode: 'resend', id: result.data?.id };
  } catch (err) {
    console.error('[email] Resend threw:', err);
    return { ok: false, mode: 'resend', error: err instanceof Error ? err.message : 'unknown' };
  }
}

/**
 * Stub mode: log the payload + dump it to ./tmp/emails so devs can inspect.
 * The .eml format is recognised by most mail clients for visual preview.
 */
function stubSend(params: SendEmailParams): SendEmailResult {
  const recipients = Array.isArray(params.to) ? params.to.join(', ') : params.to;
  console.log('\n========== EMAIL (stub mode — RESEND_API_KEY not set) ==========');
  console.log(`To:      ${recipients}`);
  console.log(`From:    ${FROM_ADDRESS}`);
  console.log(`Subject: ${params.subject}`);
  if (params.attachments?.length) {
    console.log(`Attachments: ${params.attachments.map((a) => a.filename).join(', ')}`);
  }
  console.log('================================================================\n');

  try {
    if (!fs.existsSync(STUB_DIR)) fs.mkdirSync(STUB_DIR, { recursive: true });
    const filename = `${Date.now()}_${recipients.replace(/[^a-z0-9]+/gi, '-')}.eml`;
    const eml = [
      `From: ${FROM_ADDRESS}`,
      `To: ${recipients}`,
      `Subject: ${params.subject}`,
      'MIME-Version: 1.0',
      'Content-Type: text/html; charset=utf-8',
      '',
      params.html,
    ].join('\r\n');
    fs.writeFileSync(path.join(STUB_DIR, filename), eml, 'utf-8');
  } catch (err) {
    console.error('[email] stub write failed:', err);
  }

  return { ok: true, mode: 'stub', id: `stub_${Date.now()}` };
}

/** Convenience export so consumers can branch on mode without importing env. */
export const isStubMode = (): boolean => !resend;
