CREATE TABLE IF NOT EXISTS account_summary
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    account_number
    VARCHAR
(
    10
) NOT NULL,
    current_balance NUMERIC
(
    10,
    2
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS account_transaction
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    timestamp
    TIMESTAMP
    NOT
    NULL,
    amount
    NUMERIC
(
    8,
    2
) NOT NULL,
    account_summary_id INTEGER NOT NULL,
    CONSTRAINT fk_transaction_account_summary
    FOREIGN KEY
(
    account_summary_id
)
    REFERENCES account_summary
(
    id
)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    );

CREATE INDEX IF NOT EXISTS idx_account_transaction_account_summary_id
    ON account_transaction (account_summary_id);