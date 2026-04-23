-- Migration: 0050_create_split_payments.sql
-- Description: Enable splitting a booking's cost across multiple payers at checkout
-- Part of v2-practical-ux: "pay your share" at booking time instead of chasing friends.
-- Statuses: pending (created) -> awaiting (notified) -> paid | refunded | expired

-- UP
CREATE TYPE split_payment_status AS ENUM (
    'pending',
    'awaiting',
    'paid',
    'refunded',
    'expired'
);

CREATE TABLE split_payments (
    id              BIGSERIAL              PRIMARY KEY,
    booking_id      BIGINT                 NOT NULL REFERENCES bookings(id) ON DELETE CASCADE ON UPDATE CASCADE,
    payer_user_id   BIGINT                 NOT NULL REFERENCES users(id)    ON DELETE RESTRICT ON UPDATE CASCADE,
    amount          NUMERIC(10,2)          NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3)             NOT NULL DEFAULT 'EUR',
    status          split_payment_status   NOT NULL DEFAULT 'pending',
    stripe_payment_intent_id VARCHAR(255),
    paid_at         TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_split_payment_booking_payer UNIQUE (booking_id, payer_user_id)
);

CREATE INDEX CONCURRENTLY idx_split_payments_booking  ON split_payments (booking_id);
CREATE INDEX CONCURRENTLY idx_split_payments_payer    ON split_payments (payer_user_id);
CREATE INDEX CONCURRENTLY idx_split_payments_status   ON split_payments (status);
CREATE INDEX CONCURRENTLY idx_split_payments_expires  ON split_payments (expires_at) WHERE status = 'awaiting';

-- DOWN
DROP INDEX IF EXISTS idx_split_payments_expires;
DROP INDEX IF EXISTS idx_split_payments_status;
DROP INDEX IF EXISTS idx_split_payments_payer;
DROP INDEX IF EXISTS idx_split_payments_booking;
DROP TABLE IF EXISTS split_payments;
DROP TYPE IF EXISTS split_payment_status;
