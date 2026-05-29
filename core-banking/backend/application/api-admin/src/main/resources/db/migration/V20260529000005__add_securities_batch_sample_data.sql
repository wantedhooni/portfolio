-- ============================================================
--  증권 배치 샘플 데이터
--
--  목적: SecuritiesSettlementBatchJob (정산 + 원장 전기 2-Step) 테스트
--
--  데이터 구성:
--    1. account_tx  — BUY/SELL 체결 거래 3,000건 (최근 90일 분산)
--       · status = COMPLETED, reference_id = 'BATCH-TRD-{n}'
--       · 정산 배치가 Settlement 생성 → 원장 전기까지 처리
--    2. stock_order — 위 체결에 대응하는 주문 레코드 3,000건
--       · status = FILLED
--
--  멱등성: reference_id 기준 중복 방지
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. 체결 거래 (account_tx) — BUY/SELL 3,000건
-- ─────────────────────────────────────────────────────────────
WITH
account_pool AS (
    SELECT id, COUNT(*) OVER () AS cnt, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM account
),
stock_pool AS (
    SELECT id, last_price, COUNT(*) OVER () AS cnt, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM stock WHERE is_active = true
)
INSERT INTO account_tx (
    created_at, updated_at,
    account_id, stock_id, tx_type,
    amount, quantity, price, fee, tax,
    status, reference_id, traded_at
)
SELECT
    now() - ((n % 90)   || ' days')::interval
            - ((n % 1440) || ' minutes')::interval,
    now() - ((n % 90)   || ' days')::interval
            - ((n % 1440) || ' minutes')::interval,
    -- 계좌: 순환 배정
    (SELECT id FROM account_pool WHERE rn = (n % (SELECT cnt FROM account_pool LIMIT 1)) + 1),
    -- 종목: 순환 배정
    (SELECT id FROM stock_pool   WHERE rn = (n % (SELECT cnt FROM stock_pool   LIMIT 1)) + 1),
    CASE WHEN n % 2 = 0 THEN 'BUY' ELSE 'SELL' END,
    -- amount: BUY = 음수(출금), SELL = 양수(입금)
    CASE WHEN n % 2 = 0
         THEN -(((SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1)
                  * ((n % 50) + 1)) + 1500)
         ELSE  (((SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1)
                  * ((n % 50) + 1)) - 1500
                  - ROUND((SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1)
                           * ((n % 50) + 1) * 0.0023, 4))
    END,
    (n % 50) + 1,     -- quantity: 1~50주
    (SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1),
    1500,             -- fee: 고정 1,500원
    CASE WHEN n % 2 = 0 THEN 0
         ELSE ROUND((SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1)
                    * ((n % 50) + 1) * 0.0023, 4)
    END,              -- tax: SELL 시 거래세 0.23%, BUY = 0
    'COMPLETED',
    'BATCH-TRD-' || lpad(n::text, 7, '0'),
    now() - ((n % 90)   || ' days')::interval
            - ((n % 1440) || ' minutes')::interval
FROM generate_series(1, 3000) AS s(n)
WHERE EXISTS (SELECT 1 FROM account_pool)
  AND EXISTS (SELECT 1 FROM stock_pool)
  AND NOT EXISTS (
      SELECT 1 FROM account_tx tx
      WHERE tx.reference_id = 'BATCH-TRD-' || lpad(s.n::text, 7, '0')
  );


-- ─────────────────────────────────────────────────────────────
-- 2. 주문 (stock_order) — account_tx 3,000건 대응
--    · status = FILLED, reference_id 동일
-- ─────────────────────────────────────────────────────────────
WITH
account_pool AS (
    SELECT id, COUNT(*) OVER () AS cnt, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM account
),
stock_pool AS (
    SELECT id, last_price, COUNT(*) OVER () AS cnt, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM stock WHERE is_active = true
)
INSERT INTO stock_order (
    created_at, updated_at, version,
    account_id, stock_id, side, order_type,
    quantity, limit_price, filled_quantity, avg_fill_price,
    status, reference_id,
    ordered_at, filled_at, cancelled_at
)
SELECT
    now() - ((n % 90)   || ' days')::interval - ((n % 1440) || ' minutes')::interval,
    now() - ((n % 90)   || ' days')::interval - ((n % 1440) || ' minutes')::interval + interval '5 seconds',
    0,
    (SELECT id FROM account_pool WHERE rn = (n % (SELECT cnt FROM account_pool LIMIT 1)) + 1),
    (SELECT id FROM stock_pool   WHERE rn = (n % (SELECT cnt FROM stock_pool   LIMIT 1)) + 1),
    CASE WHEN n % 2 = 0 THEN 'BUY' ELSE 'SELL' END,
    CASE WHEN n % 3 = 0 THEN 'LIMIT' ELSE 'MARKET' END,
    (n % 50) + 1,
    CASE WHEN n % 3 = 0
         THEN (SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1)
         ELSE NULL END,
    (n % 50) + 1,
    (SELECT last_price FROM stock_pool WHERE rn = (n % (SELECT cnt FROM stock_pool LIMIT 1)) + 1),
    'FILLED',
    'BATCH-TRD-' || lpad(n::text, 7, '0'),
    now() - ((n % 90)   || ' days')::interval - ((n % 1440) || ' minutes')::interval,
    now() - ((n % 90)   || ' days')::interval - ((n % 1440) || ' minutes')::interval + interval '5 seconds',
    NULL
FROM generate_series(1, 3000) AS s(n)
WHERE EXISTS (SELECT 1 FROM account_pool)
  AND EXISTS (SELECT 1 FROM stock_pool)
  AND NOT EXISTS (
      SELECT 1 FROM stock_order so
      WHERE so.reference_id = 'BATCH-TRD-' || lpad(s.n::text, 7, '0')
  );


-- ─────────────────────────────────────────────────────────────
-- 확인
-- ─────────────────────────────────────────────────────────────
DO $$
DECLARE
    v_trade INT;
    v_order INT;
BEGIN
    SELECT COUNT(*) INTO v_trade FROM account_tx  WHERE reference_id LIKE 'BATCH-TRD-%';
    SELECT COUNT(*) INTO v_order FROM stock_order WHERE reference_id LIKE 'BATCH-TRD-%';
    RAISE NOTICE '[증권 배치 샘플] 체결거래=% 주문=%', v_trade, v_order;
END $$;
