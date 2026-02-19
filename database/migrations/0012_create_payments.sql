-- Migration: 0012_create_payments.sql
-- Description: Create payments table linked to bookings
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE payments (
    id                  BIGSERIAL       PRIMARY KEY,
    booking_id          BIGINT          NOT NULL
                            REFERENCES bookings (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    payer_id            BIGINT          NOT NULL
                            REFERENCES users (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    amount              NUMERIC(10,2)   NOT NULL
                            CHECK (amount > 0),
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status              payment_status  NOT NULL DEFAULT 'pending',
    payment_method      VARCHAR(50),
    external_payment_id VARCHAR(255),
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_payments_booking_id ON payments (booking_id);
CREATE INDEX CONCURRENTLY idx_payments_payer_id ON payments (payer_id);
CREATE INDEX CONCURRENTLY idx_payments_status ON payments (status);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_payments_status;
DROP INDEX IF EXISTS idx_payments_payer_id;
DROP INDEX IF EXISTS idx_payments_booking_id;
DROP TABLE IF EXISTS payments;
