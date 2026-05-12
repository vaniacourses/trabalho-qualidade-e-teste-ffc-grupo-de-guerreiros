CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL UNIQUE,
    password_hash   VARCHAR(72)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE SEQUENCE account_number_seq START 1;

CREATE TABLE accounts (
    id          BIGSERIAL PRIMARY KEY,
    number      VARCHAR(20)    NOT NULL UNIQUE,
    balance     NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    user_id     BIGINT         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE transactions (
    id                  BIGSERIAL PRIMARY KEY,
    source_account      BIGINT         REFERENCES accounts (id),
    destination_account BIGINT         REFERENCES accounts (id),
    type                VARCHAR(20)    NOT NULL,
    amount              NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    date                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT transactions_type_check CHECK (type IN ('deposit', 'withdraw', 'transfer', 'investment', 'redemption'))
);

CREATE INDEX idx_transaction_source      ON transactions (source_account, date DESC);
CREATE INDEX idx_transaction_destination ON transactions (destination_account, date DESC);

CREATE TABLE investments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    amount      NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
    last_update TIMESTAMPTZ    NOT NULL DEFAULT now()
);
