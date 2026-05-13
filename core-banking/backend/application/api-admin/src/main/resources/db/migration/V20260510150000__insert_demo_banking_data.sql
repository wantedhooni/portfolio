WITH user_seed AS (
    SELECT
        generate_series(1, 30) AS user_idx
)
INSERT INTO "users" (created_at, updated_at, email, password, name, role)
SELECT
    now(),
    now(),
    concat('demo.user', lpad(user_idx::text, 2, '0'), '@example.com'),
    '$2a$10$WeuYkeJeQJVlGZ8gNPCCue4wUqEY3Z6De6QyX8hKTx9KtQpCA0.5u',
    concat('Demo User ', lpad(user_idx::text, 2, '0')),
    'USER'
FROM user_seed
ON CONFLICT (email) DO NOTHING;

WITH bank_seed (name, national_code, swift_code) AS (
    VALUES
        ('Kookmin Bank', '004', 'CZNBKRSE'),
        ('Shinhan Bank', '088', 'SHBKKRSE'),
        ('Hana Bank', '081', 'KOEXKRSE'),
        ('Woori Bank', '020', 'HVBKKRSE'),
        ('NH Bank', '011', 'NACFKRSE')
)
INSERT INTO bank (created_at, updated_at, name, national_code, swift_code)
SELECT
    now(),
    now(),
    name,
    national_code,
    swift_code
FROM bank_seed
ON CONFLICT (national_code) DO UPDATE
SET
    updated_at = excluded.updated_at,
    name = excluded.name,
    swift_code = excluded.swift_code;

WITH bank_seed (bank_idx, national_code) AS (
    VALUES
        (1, '004'),
        (2, '088'),
        (3, '081'),
        (4, '020'),
        (5, '011')
),
account_seed AS (
    SELECT
        u.user_idx,
        a.account_seq,
        ((u.user_idx + a.account_seq - 2) % 5) + 1 AS bank_idx,
        concat('demo.user', lpad(u.user_idx::text, 2, '0'), '@example.com') AS email,
        concat('Demo User ', lpad(u.user_idx::text, 2, '0')) AS owner_name
    FROM generate_series(1, 30) AS u(user_idx)
    CROSS JOIN generate_series(1, 2) AS a(account_seq)
),
account_numbers AS (
    SELECT
        account_seed.*,
        bank_seed.national_code,
        concat(
            bank_seed.national_code,
            '-',
            lpad(account_seq::text, 2, '0'),
            '-',
            lpad((user_idx * 100 + account_seq)::text, 6, '0'),
            '-',
            ((user_idx + account_seq) % 10)::text
        ) AS account_number
    FROM account_seed
    JOIN bank_seed ON bank_seed.bank_idx = account_seed.bank_idx
),
tx_delta AS (
    SELECT
        account_number,
        tx_idx,
        CASE
            WHEN tx_idx IN (1, 2, 4, 7, 10, 13)
                THEN (200000 + user_idx * 10000 + account_seq * 5000 + tx_idx * 1000)::numeric
            ELSE -(10000 + account_seq * 2000 + tx_idx * 500)::numeric
        END AS delta
    FROM account_numbers
    CROSS JOIN generate_series(1, 15) AS tx(tx_idx)
),
account_balance AS (
    SELECT
        account_number,
        sum(delta) AS balance
    FROM tx_delta
    GROUP BY account_number
)
INSERT INTO account (
    created_at,
    updated_at,
    bank_id,
    user_id,
    version,
    account_number,
    owner_name,
    balance,
    status
)
SELECT
    now(),
    now(),
    bank.id,
    "users".id,
    0,
    account_numbers.account_number,
    account_numbers.owner_name,
    account_balance.balance,
    'ACTIVE'
FROM account_numbers
JOIN account_balance ON account_balance.account_number = account_numbers.account_number
JOIN bank ON bank.national_code = account_numbers.national_code
JOIN "users" ON "users".email = account_numbers.email
ON CONFLICT (account_number) DO UPDATE
SET
    updated_at = excluded.updated_at,
    bank_id = excluded.bank_id,
    user_id = excluded.user_id,
    owner_name = excluded.owner_name,
    balance = excluded.balance,
    status = excluded.status;

WITH bank_seed (bank_idx, national_code) AS (
    VALUES
        (1, '004'),
        (2, '088'),
        (3, '081'),
        (4, '020'),
        (5, '011')
),
account_seed AS (
    SELECT
        u.user_idx,
        a.account_seq,
        ((u.user_idx + a.account_seq - 2) % 5) + 1 AS bank_idx
    FROM generate_series(1, 30) AS u(user_idx)
    CROSS JOIN generate_series(1, 2) AS a(account_seq)
),
account_numbers AS (
    SELECT
        account_seed.*,
        concat(
            bank_seed.national_code,
            '-',
            lpad(account_seq::text, 2, '0'),
            '-',
            lpad((user_idx * 100 + account_seq)::text, 6, '0'),
            '-',
            ((user_idx + account_seq) % 10)::text
        ) AS account_number
    FROM account_seed
    JOIN bank_seed ON bank_seed.bank_idx = account_seed.bank_idx
),
tx_source AS (
    SELECT
        account_number,
        tx_idx,
        CASE
            WHEN tx_idx IN (1, 2, 4, 7, 10, 13) THEN 'DEPOSIT'
            ELSE 'WITHDRAW'
        END AS type,
        CASE
            WHEN tx_idx IN (1, 2, 4, 7, 10, 13)
                THEN (200000 + user_idx * 10000 + account_seq * 5000 + tx_idx * 1000)::numeric
            ELSE (10000 + account_seq * 2000 + tx_idx * 500)::numeric
        END AS amount
    FROM account_numbers
    CROSS JOIN generate_series(1, 15) AS tx(tx_idx)
),
tx_with_balance AS (
    SELECT
        account_number,
        tx_idx,
        type,
        amount,
        sum(CASE WHEN type = 'DEPOSIT' THEN amount ELSE -amount END)
            OVER (PARTITION BY account_number ORDER BY tx_idx) AS balance_snapshot,
        format('Demo tx #%s', lpad(tx_idx::text, 2, '0')) AS description
    FROM tx_source
)
INSERT INTO account_transaction (
    created_at,
    updated_at,
    account_id,
    type,
    amount,
    balance_snapshot,
    description
)
SELECT
    now() - ((16 - tx_with_balance.tx_idx) * interval '1 day'),
    now() - ((16 - tx_with_balance.tx_idx) * interval '1 day'),
    account.id,
    tx_with_balance.type,
    tx_with_balance.amount,
    tx_with_balance.balance_snapshot,
    tx_with_balance.description
FROM tx_with_balance
JOIN account ON account.account_number = tx_with_balance.account_number
WHERE NOT EXISTS (
    SELECT 1
    FROM account_transaction existing_tx
    WHERE existing_tx.account_id = account.id
      AND existing_tx.description = tx_with_balance.description
);
