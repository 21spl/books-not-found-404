-- Add optimistic-locking version columns.

ALTER TABLE books
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE cart
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE cart_item
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE purchase
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


-- Add payment tracking.
-- Existing purchases are treated as historical, completed purchases.

ALTER TABLE purchase
    ADD COLUMN payment_status VARCHAR(30) NOT NULL DEFAULT 'PAID';

ALTER TABLE purchase
    ADD COLUMN stripe_session_id VARCHAR(255);


-- A Stripe Checkout Session can belong to at most one purchase.

CREATE UNIQUE INDEX uk_purchase_stripe_session_id
    ON purchase (stripe_session_id)
    WHERE stripe_session_id IS NOT NULL;


-- Available inventory must never become negative.

ALTER TABLE books
    ADD CONSTRAINT chk_books_copies_nonnegative
    CHECK (copies >= 0);