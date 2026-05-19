-- =====================================================================
-- 대용량 데모 데이터 시드 (Idempotent)
-- 비밀번호: 모든 데모 사용자/관리자 = "Qwer1234!"
-- 정합성: account.balance = deposits - buy_total + sell_total + dividend_total
--         stock_position.total_quantity = sum(lot.remaining_quantity)
--         stock_position.realized_pnl = sum(lot_disposal.realized_pnl)
-- =====================================================================

-- 결정적 randomness — 재실행 시 동일 데이터
SELECT setseed(0.42);

DO $$
DECLARE
    bcrypt_hash CONSTANT TEXT := '$2y$10$erfFUza3AAbMP/Y3Pqkd0ORMZSF6h4XXQGxFkHvBOcvoBttiDzRsy';
    user_count   CONSTANT INT := 100;
    admin_count  CONSTANT INT := 5;
BEGIN
    RAISE NOTICE '[demo-data] start (users=%, admins=%)', user_count, admin_count;
END $$;

-- =====================================================================
-- 1. ADMINS (5명)
-- =====================================================================
INSERT INTO admin (created_at, updated_at, email, password, name, role)
SELECT
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval,
    'admin' || lpad(n::text, 2, '0') || '@example.com',
    '$2y$10$erfFUza3AAbMP/Y3Pqkd0ORMZSF6h4XXQGxFkHvBOcvoBttiDzRsy',
    (ARRAY['김운영','이관리','박감독','최총괄','정책임'])[n],
    'ROLE_ADMIN'
FROM generate_series(1, 5) AS s(n)
WHERE NOT EXISTS (
    SELECT 1 FROM admin a
    WHERE a.email = 'admin' || lpad(s.n::text, 2, '0') || '@example.com'
);

-- =====================================================================
-- 2. USERS (100명)
-- =====================================================================
WITH name_pool AS (
    SELECT ARRAY[
        '김민준','이서연','박지호','최예린','정수현','강도윤','조하은','윤지우','임서윤','한주원',
        '오시우','신유나','권민서','황시현','안채원','송지안','류건우','전서아','홍준서','문하린',
        '서지율','배수아','남도현','노예진','구지훈','심다은','구하준','백은우','허지원','유재민'
    ] AS names
)
INSERT INTO users (created_at, updated_at, email, password, name, role)
SELECT
    now() - (n * 7 || ' hours')::interval,
    now() - (n * 7 || ' hours')::interval,
    'demo.user' || lpad(n::text, 3, '0') || '@example.com',
    '$2y$10$erfFUza3AAbMP/Y3Pqkd0ORMZSF6h4XXQGxFkHvBOcvoBttiDzRsy',
    (SELECT names[((n - 1) % cardinality(names)) + 1] FROM name_pool)
        || lpad(n::text, 3, '0'),
    'USER'
FROM generate_series(1, 100) AS s(n)
WHERE NOT EXISTS (
    SELECT 1 FROM users u
    WHERE u.email = 'demo.user' || lpad(s.n::text, 3, '0') || '@example.com'
);

-- =====================================================================
-- 3. STOCKS (80개 — 실제 KRX/NASDAQ/NYSE 티커)
-- =====================================================================
INSERT INTO stock (created_at, updated_at, ticker, name, exchange, sector, currency, last_price, market_cap, is_active, last_synced_at)
SELECT now(), now(), v.ticker, v.name, v.exchange, v.sector, v.currency, v.last_price, v.market_cap, true, now()
FROM (VALUES
    -- KRX (40)
    ('005930','삼성전자','KRX','반도체','KRW',75000,4500000000000000),
    ('000660','SK하이닉스','KRX','반도체','KRW',145000,1050000000000000),
    ('373220','LG에너지솔루션','KRX','2차전지','KRW',380000,890000000000000),
    ('207940','삼성바이오로직스','KRX','바이오','KRW',920000,650000000000000),
    ('005380','현대차','KRX','자동차','KRW',245000,520000000000000),
    ('006400','삼성SDI','KRX','2차전지','KRW',410000,280000000000000),
    ('051910','LG화학','KRX','화학','KRW',420000,290000000000000),
    ('035420','NAVER','KRX','IT서비스','KRW',195000,310000000000000),
    ('035720','카카오','KRX','IT서비스','KRW',48000,210000000000000),
    ('068270','셀트리온','KRX','바이오','KRW',180000,250000000000000),
    ('028260','삼성물산','KRX','지주회사','KRW',145000,270000000000000),
    ('012330','현대모비스','KRX','자동차부품','KRW',230000,210000000000000),
    ('066570','LG전자','KRX','전자제품','KRW',95000,160000000000000),
    ('003550','LG','KRX','지주회사','KRW',88000,140000000000000),
    ('096770','SK이노베이션','KRX','정유','KRW',125000,120000000000000),
    ('017670','SK텔레콤','KRX','통신','KRW',54000,118000000000000),
    ('105560','KB금융','KRX','금융','KRW',72000,290000000000000),
    ('055550','신한지주','KRX','금융','KRW',46000,235000000000000),
    ('086790','하나금융지주','KRX','금융','KRW',60000,180000000000000),
    ('316140','우리금융지주','KRX','금융','KRW',15000,110000000000000),
    ('015760','한국전력','KRX','전기','KRW',23000,150000000000000),
    ('033780','KT&G','KRX','담배','KRW',105000,140000000000000),
    ('030200','KT','KRX','통신','KRW',39000,99000000000000),
    ('032830','삼성생명','KRX','보험','KRW',95000,180000000000000),
    ('009150','삼성전기','KRX','전자부품','KRW',155000,115000000000000),
    ('010130','고려아연','KRX','비철금속','KRW',650000,135000000000000),
    ('011170','롯데케미칼','KRX','화학','KRW',110000,47000000000000),
    ('011200','HMM','KRX','해운','KRW',21000,200000000000000),
    ('024110','기업은행','KRX','금융','KRW',14500,115000000000000),
    ('047810','한국항공우주','KRX','방산','KRW',56000,55000000000000),
    ('051900','LG생활건강','KRX','생활용품','KRW',320000,52000000000000),
    ('090430','아모레퍼시픽','KRX','화장품','KRW',145000,86000000000000),
    ('097950','CJ제일제당','KRX','식품','KRW',330000,49000000000000),
    ('251270','넷마블','KRX','게임','KRW',62000,53000000000000),
    ('259960','크래프톤','KRX','게임','KRW',280000,140000000000000),
    ('036570','엔씨소프트','KRX','게임','KRW',180000,40000000000000),
    ('042660','한화오션','KRX','조선','KRW',32000,98000000000000),
    ('010140','삼성중공업','KRX','조선','KRW',9800,84000000000000),
    ('329180','HD현대중공업','KRX','조선','KRW',180000,158000000000000),
    ('267260','HD현대일렉트릭','KRX','전기설비','KRW',310000,113000000000000),
    -- NASDAQ (25)
    ('AAPL','Apple Inc.','NASDAQ','Technology','USD',225,3400000000000),
    ('MSFT','Microsoft Corporation','NASDAQ','Technology','USD',420,3100000000000),
    ('GOOGL','Alphabet Inc. Class A','NASDAQ','Technology','USD',175,2150000000000),
    ('AMZN','Amazon.com Inc.','NASDAQ','Consumer Discretionary','USD',195,2050000000000),
    ('NVDA','NVIDIA Corporation','NASDAQ','Semiconductors','USD',135,3300000000000),
    ('META','Meta Platforms Inc.','NASDAQ','Technology','USD',580,1480000000000),
    ('TSLA','Tesla Inc.','NASDAQ','Automotive','USD',245,780000000000),
    ('AVGO','Broadcom Inc.','NASDAQ','Semiconductors','USD',175,820000000000),
    ('COST','Costco Wholesale','NASDAQ','Consumer Staples','USD',900,398000000000),
    ('ADBE','Adobe Inc.','NASDAQ','Software','USD',490,220000000000),
    ('NFLX','Netflix Inc.','NASDAQ','Media','USD',720,310000000000),
    ('AMD','Advanced Micro Devices','NASDAQ','Semiconductors','USD',155,250000000000),
    ('INTC','Intel Corporation','NASDAQ','Semiconductors','USD',24,103000000000),
    ('CSCO','Cisco Systems','NASDAQ','Networking','USD',56,225000000000),
    ('PEP','PepsiCo Inc.','NASDAQ','Consumer Staples','USD',165,225000000000),
    ('TMUS','T-Mobile US','NASDAQ','Telecommunications','USD',220,260000000000),
    ('QCOM','QUALCOMM Inc.','NASDAQ','Semiconductors','USD',165,184000000000),
    ('TXN','Texas Instruments','NASDAQ','Semiconductors','USD',200,182000000000),
    ('CMCSA','Comcast Corporation','NASDAQ','Media','USD',43,166000000000),
    ('MU','Micron Technology','NASDAQ','Semiconductors','USD',102,113000000000),
    ('AMAT','Applied Materials','NASDAQ','Semiconductors','USD',195,162000000000),
    ('PYPL','PayPal Holdings','NASDAQ','Financial Services','USD',85,87000000000),
    ('SBUX','Starbucks Corporation','NASDAQ','Restaurants','USD',95,108000000000),
    ('PLTR','Palantir Technologies','NASDAQ','Software','USD',65,148000000000),
    ('MRVL','Marvell Technology','NASDAQ','Semiconductors','USD',95,82000000000),
    -- NYSE (15)
    ('JPM','JPMorgan Chase','NYSE','Banking','USD',240,680000000000),
    ('V','Visa Inc.','NYSE','Financial Services','USD',290,580000000000),
    ('MA','Mastercard Inc.','NYSE','Financial Services','USD',495,460000000000),
    ('JNJ','Johnson & Johnson','NYSE','Healthcare','USD',165,400000000000),
    ('UNH','UnitedHealth Group','NYSE','Healthcare','USD',595,548000000000),
    ('XOM','Exxon Mobil','NYSE','Energy','USD',115,510000000000),
    ('PG','Procter & Gamble','NYSE','Consumer Staples','USD',172,406000000000),
    ('HD','Home Depot','NYSE','Retail','USD',410,408000000000),
    ('BAC','Bank of America','NYSE','Banking','USD',45,348000000000),
    ('KO','Coca-Cola','NYSE','Beverages','USD',69,298000000000),
    ('WMT','Walmart Inc.','NYSE','Retail','USD',95,765000000000),
    ('CVX','Chevron Corporation','NYSE','Energy','USD',155,283000000000),
    ('LLY','Eli Lilly and Company','NYSE','Pharmaceuticals','USD',900,860000000000),
    ('ABBV','AbbVie Inc.','NYSE','Pharmaceuticals','USD',195,344000000000),
    ('MRK','Merck & Co.','NYSE','Pharmaceuticals','USD',95,242000000000)
) AS v(ticker, name, exchange, sector, currency, last_price, market_cap)
ON CONFLICT (ticker, exchange) DO NOTHING;


-- =====================================================================
-- 4. ACCOUNTS (사용자당 1-3개, 총 ~200개)
-- =====================================================================
WITH user_pool AS (
    SELECT id, email,
           1 + (abs(hashtext(email || 'cnt')) % 3) AS acct_count
    FROM users
    WHERE email LIKE 'demo.user%'
),
account_seed AS (
    SELECT
        u.id AS user_id,
        u.email,
        g.idx AS acct_idx,
        'ACC-DEMO-' || lpad(u.id::text, 4, '0') || '-' || g.idx AS account_number,
        CASE (g.idx % 2) WHEN 0 THEN 'VIRTUAL' ELSE 'REAL' END AS account_type,
        CASE
            WHEN (abs(hashtext(u.email || 'cur')) % 10) < 8 THEN 'KRW'
            ELSE 'USD'
        END AS currency,
        now() - (u.id * 3 || ' days')::interval AS created_at
    FROM user_pool u
    CROSS JOIN LATERAL generate_series(1, u.acct_count) AS g(idx)
)
INSERT INTO account (
    created_at, updated_at, version,
    user_id, account_number, account_name, account_type, currency,
    balance, available_balance, status
)
SELECT
    a.created_at, a.created_at, 0,
    a.user_id,
    a.account_number,
    CASE a.acct_idx
        WHEN 1 THEN '주식 계좌'
        WHEN 2 THEN '예비 계좌'
        ELSE '연금 계좌'
    END,
    a.account_type,
    a.currency,
    0,  -- 입금 후 갱신
    0,
    CASE
        WHEN (abs(hashtext(a.account_number || 'status')) % 100) < 90 THEN 'ACTIVE'
        WHEN (abs(hashtext(a.account_number || 'status')) % 100) < 96 THEN 'SUSPENDED'
        ELSE 'CLOSED'
    END
FROM account_seed a
WHERE NOT EXISTS (
    SELECT 1 FROM account x WHERE x.account_number = a.account_number
);


-- =====================================================================
-- 5. 초기 DEPOSIT 거래 (계좌당 1건, 30M~200M)
-- =====================================================================
INSERT INTO account_tx (
    created_at, updated_at,
    account_id, stock_id, tx_type, amount, quantity, price, fee, tax, status, reference_id, traded_at
)
SELECT
    a.created_at + interval '1 hour',
    a.created_at + interval '1 hour',
    a.id,
    NULL,
    'DEPOSIT',
    -- 30M~200M 사이 (현실적 초기 자금)
    (30000000 + (abs(hashtext(a.account_number || 'dep')) % 170000000))::numeric,
    NULL, NULL, 0, 0, 'COMPLETED',
    'DEMO-DEP-' || a.id,
    a.created_at + interval '1 hour'
FROM account a
WHERE a.account_number LIKE 'ACC-DEMO-%'
  AND NOT EXISTS (
    SELECT 1 FROM account_tx t WHERE t.reference_id = 'DEMO-DEP-' || a.id
  );


-- =====================================================================
-- 6. BUY 거래 (계좌당 30-60건)
-- =====================================================================
WITH active_accounts AS (
    SELECT id, account_number, currency, created_at,
           30 + (abs(hashtext(account_number || 'buycnt')) % 31) AS buy_count
    FROM account
    WHERE account_number LIKE 'ACC-DEMO-%'
      AND status = 'ACTIVE'
),
stock_pool AS (
    SELECT id, last_price, currency,
           ROW_NUMBER() OVER (PARTITION BY currency ORDER BY id) AS rn
    FROM stock
),
stock_totals AS (
    SELECT currency, COUNT(*) AS total FROM stock GROUP BY currency
),
buy_plan AS (
    SELECT
        a.id AS account_id,
        a.account_number,
        a.currency,
        g.idx AS buy_idx,
        ((abs(hashtext(a.account_number || g.idx::text || 'stk'))) % st.total) + 1 AS stock_rn,
        (now() - ((abs(hashtext(a.account_number || g.idx::text || 't')) % 365) || ' days')::interval
              - ((abs(hashtext(a.account_number || g.idx::text || 'th')) % 24) || ' hours')::interval) AS traded_at
    FROM active_accounts a
    CROSS JOIN LATERAL generate_series(1, a.buy_count) AS g(idx)
    JOIN stock_totals st ON st.currency = a.currency
),
buy_with_stock AS (
    SELECT
        bp.account_id,
        bp.account_number,
        bp.currency,
        bp.buy_idx,
        bp.traded_at,
        sp.id AS stock_id,
        (sp.last_price * (0.85 + (abs(hashtext(bp.account_number || bp.buy_idx::text || 'p')) % 30) / 100.0))::numeric(20, 4) AS actual_price,
        CASE
            WHEN bp.currency = 'USD' THEN (1 + (abs(hashtext(bp.account_number || bp.buy_idx::text || 'q')) % 20))::numeric
            WHEN sp.last_price >= 500000 THEN (1 + (abs(hashtext(bp.account_number || bp.buy_idx::text || 'q')) % 5))::numeric
            WHEN sp.last_price >= 100000 THEN (1 + (abs(hashtext(bp.account_number || bp.buy_idx::text || 'q')) % 30))::numeric
            ELSE (10 + (abs(hashtext(bp.account_number || bp.buy_idx::text || 'q')) % 200))::numeric
        END AS qty
    FROM buy_plan bp
    JOIN stock_pool sp ON sp.currency = bp.currency AND sp.rn = bp.stock_rn
)
INSERT INTO account_tx (
    created_at, updated_at,
    account_id, stock_id, tx_type, amount, quantity, price, fee, tax, status, reference_id, traded_at
)
SELECT
    bws.traded_at, bws.traded_at,
    bws.account_id,
    bws.stock_id,
    'BUY',
    -- amount = -(price * qty + fee + tax) — 매수는 출금
    -((bws.actual_price * bws.qty)
        + (CASE WHEN bws.currency = 'USD' THEN bws.actual_price * bws.qty * 0.001 ELSE bws.actual_price * bws.qty * 0.00015 END)),
    bws.qty,
    bws.actual_price,
    (CASE WHEN bws.currency = 'USD' THEN bws.actual_price * bws.qty * 0.001 ELSE bws.actual_price * bws.qty * 0.00015 END)::numeric(10, 4),
    0::numeric(10, 4),
    'COMPLETED',
    'DEMO-BUY-' || bws.account_number || '-' || bws.buy_idx,
    bws.traded_at
FROM buy_with_stock bws
WHERE NOT EXISTS (
    SELECT 1 FROM account_tx t
    WHERE t.reference_id = 'DEMO-BUY-' || bws.account_number || '-' || bws.buy_idx
);


-- =====================================================================
-- 7. STOCK_POSITION (account × stock 별 집계)
-- =====================================================================
INSERT INTO stock_position (
    created_at, updated_at, version,
    account_id, stock_id, total_quantity, realized_pnl
)
SELECT
    MIN(t.traded_at), MAX(t.traded_at), 0,
    t.account_id,
    t.stock_id,
    SUM(t.quantity)::numeric(20, 8),  -- 매수 전체 (매도는 이후 차감)
    0
FROM account_tx t
WHERE t.tx_type = 'BUY'
  AND t.reference_id LIKE 'DEMO-BUY-%'
GROUP BY t.account_id, t.stock_id
ON CONFLICT (account_id, stock_id) DO NOTHING;


-- =====================================================================
-- 8. POSITION_LOT (매수 1건당 1개 로트)
-- =====================================================================
INSERT INTO position_lot (
    created_at, updated_at, version,
    position_id, buy_tx_id, original_quantity, remaining_quantity, buy_price, bought_at, lot_status
)
SELECT
    t.traded_at, t.traded_at, 0,
    p.id,
    t.id,
    t.quantity,
    t.quantity,  -- 매도 처분 시 이후 단계에서 감소
    t.price,
    t.traded_at,
    'OPEN'
FROM account_tx t
JOIN stock_position p
  ON p.account_id = t.account_id AND p.stock_id = t.stock_id
WHERE t.tx_type = 'BUY'
  AND t.reference_id LIKE 'DEMO-BUY-%'
  AND NOT EXISTS (
    SELECT 1 FROM position_lot l WHERE l.buy_tx_id = t.id
  );


-- =====================================================================
-- 9. SELL 거래 + LOT_DISPOSAL (각 계좌 매수 로트의 ~25% 처분)
-- =====================================================================
WITH sellable_lots AS (
    -- 각 계좌별 매수 로트 중 25% 정도를 매도 대상으로 선정
    SELECT
        l.id AS lot_id,
        l.position_id,
        l.buy_tx_id,
        l.buy_price,
        l.original_quantity,
        l.bought_at,
        t.account_id,
        t.stock_id,
        s.currency,
        s.last_price,
        ROW_NUMBER() OVER (PARTITION BY t.account_id ORDER BY l.id) AS rn,
        COUNT(*) OVER (PARTITION BY t.account_id) AS lot_total
    FROM position_lot l
    JOIN account_tx t ON t.id = l.buy_tx_id
    JOIN stock s ON s.id = t.stock_id
    WHERE l.lot_status = 'OPEN'
      AND t.reference_id LIKE 'DEMO-BUY-%'
),
to_sell AS (
    SELECT *,
        -- 매도 단가: 매수가의 0.75~1.40배 (현실적 손익 분포)
        (buy_price * (0.75 + (abs(hashtext(lot_id::text || 'sp')) % 65) / 100.0))::numeric(20, 4) AS sell_price,
        -- 매도시각: 매수 이후 7~180일 사이
        (bought_at + ((7 + (abs(hashtext(lot_id::text || 'st')) % 173)) || ' days')::interval) AS sold_at
    FROM sellable_lots
    WHERE (rn::float / lot_total) <= 0.25  -- 상위 25% 로트만 매도
),
inserted_sell_txs AS (
    INSERT INTO account_tx (
        created_at, updated_at,
        account_id, stock_id, tx_type, amount, quantity, price, fee, tax, status, reference_id, traded_at
    )
    SELECT
        sold_at, sold_at,
        account_id, stock_id, 'SELL',
        -- amount = price * qty - fee - tax (입금)
        (sell_price * original_quantity)
          - (CASE WHEN currency = 'USD' THEN sell_price * original_quantity * 0.001
                  ELSE sell_price * original_quantity * 0.00015 END)
          - (CASE WHEN currency = 'USD' THEN 0
                  ELSE sell_price * original_quantity * 0.002 END),  -- 매도세 0.2% (KRW)
        original_quantity,
        sell_price,
        (CASE WHEN currency = 'USD' THEN sell_price * original_quantity * 0.001
              ELSE sell_price * original_quantity * 0.00015 END)::numeric(10, 4),
        (CASE WHEN currency = 'USD' THEN 0
              ELSE sell_price * original_quantity * 0.002 END)::numeric(10, 4),
        'COMPLETED',
        'DEMO-SELL-' || lot_id,
        sold_at
    FROM to_sell
    WHERE NOT EXISTS (
        SELECT 1 FROM account_tx t WHERE t.reference_id = 'DEMO-SELL-' || to_sell.lot_id
    )
    RETURNING id, reference_id, traded_at
),
-- LOT_DISPOSAL: 매도된 로트 전량 처분
inserted_disposals AS (
    INSERT INTO lot_disposal (
        created_at, updated_at,
        lot_id, sell_tx_id, disposed_quantity, sell_price, realized_pnl, disposed_at
    )
    SELECT
        ts.sold_at, ts.sold_at,
        ts.lot_id,
        st.id,
        ts.original_quantity,
        ts.sell_price,
        (ts.sell_price - ts.buy_price) * ts.original_quantity,
        ts.sold_at
    FROM to_sell ts
    JOIN inserted_sell_txs st ON st.reference_id = 'DEMO-SELL-' || ts.lot_id
    WHERE NOT EXISTS (
        SELECT 1 FROM lot_disposal d WHERE d.lot_id = ts.lot_id AND d.sell_tx_id = st.id
    )
    RETURNING lot_id, realized_pnl, disposed_quantity
)
SELECT count(*) FROM inserted_disposals;


-- =====================================================================
-- 10. 처분된 로트 상태/잔량 갱신 (CLOSED, remaining = 0)
-- =====================================================================
UPDATE position_lot l
SET remaining_quantity = 0,
    lot_status = 'CLOSED',
    updated_at = d.disposed_at
FROM lot_disposal d
WHERE d.lot_id = l.id
  AND l.lot_status = 'OPEN'
  AND l.remaining_quantity = l.original_quantity;


-- =====================================================================
-- 11. STOCK_POSITION 집계 재계산 (total_quantity, realized_pnl)
-- =====================================================================
UPDATE stock_position p
SET total_quantity = COALESCE(agg.total_qty, 0),
    realized_pnl   = COALESCE(agg.total_pnl, 0),
    updated_at     = now()
FROM (
    SELECT
        l.position_id,
        SUM(l.remaining_quantity) AS total_qty,
        COALESCE((
            SELECT SUM(d.realized_pnl)
            FROM lot_disposal d
            JOIN position_lot l2 ON l2.id = d.lot_id
            WHERE l2.position_id = l.position_id
        ), 0) AS total_pnl
    FROM position_lot l
    GROUP BY l.position_id
) agg
WHERE agg.position_id = p.id;


-- =====================================================================
-- 12. DIVIDEND 거래 (활성 포지션 보유 종목에 대해 일부 배당 지급)
-- =====================================================================
INSERT INTO account_tx (
    created_at, updated_at,
    account_id, stock_id, tx_type, amount, quantity, price, fee, tax, status, reference_id, traded_at
)
SELECT
    div_at, div_at,
    p.account_id,
    p.stock_id,
    'DIVIDEND',
    -- 배당금: 보유수량 × 가격 × 1.5% (총액)
    (p.total_quantity * s.last_price * 0.015)::numeric(20, 4),
    NULL,
    NULL,
    0,
    -- 배당소득세 15.4% (한국) / 0 (해외)
    (CASE WHEN s.currency = 'KRW' THEN p.total_quantity * s.last_price * 0.015 * 0.154 ELSE 0 END)::numeric(10, 4),
    'COMPLETED',
    'DEMO-DIV-' || p.id,
    div_at
FROM stock_position p
JOIN stock s ON s.id = p.stock_id
CROSS JOIN LATERAL (SELECT now() - ((abs(hashtext(p.id::text || 'div')) % 90) || ' days')::interval AS div_at) d
WHERE p.total_quantity > 0
  AND (abs(hashtext(p.id::text || 'div_select')) % 100) < 40  -- 40% 포지션만 배당
  AND NOT EXISTS (
    SELECT 1 FROM account_tx t WHERE t.reference_id = 'DEMO-DIV-' || p.id
  );


-- =====================================================================
-- 13. ACCOUNT 잔고 최종 갱신 (모든 트랜잭션 반영)
-- 잔고 = 입금 + 매도대금 + 배당(세후) + 매수합계(음수)
-- =====================================================================
UPDATE account a
SET balance = COALESCE(agg.balance, 0),
    available_balance = COALESCE(agg.balance, 0),
    updated_at = now()
FROM (
    SELECT
        t.account_id,
        SUM(
            CASE
                WHEN t.tx_type = 'DIVIDEND' THEN t.amount - t.tax
                ELSE t.amount  -- DEPOSIT(+), BUY(-), SELL(+), WITHDRAWAL(-) 모두 부호 일관
            END
        ) AS balance
    FROM account_tx t
    WHERE t.reference_id LIKE 'DEMO-%'
    GROUP BY t.account_id
) agg
WHERE agg.account_id = a.id
  AND a.account_number LIKE 'ACC-DEMO-%';


-- =====================================================================
-- 14. 통계
-- =====================================================================
DO $$
DECLARE
    cnt_users   INT;
    cnt_admins  INT;
    cnt_stocks  INT;
    cnt_acct    INT;
    cnt_tx      INT;
    cnt_pos     INT;
    cnt_lot     INT;
    cnt_disp    INT;
BEGIN
    SELECT count(*) INTO cnt_users FROM users WHERE email LIKE 'demo.user%';
    SELECT count(*) INTO cnt_admins FROM admin WHERE email LIKE 'admin%@example.com';
    SELECT count(*) INTO cnt_stocks FROM stock;
    SELECT count(*) INTO cnt_acct FROM account WHERE account_number LIKE 'ACC-DEMO-%';
    SELECT count(*) INTO cnt_tx FROM account_tx WHERE reference_id LIKE 'DEMO-%';
    SELECT count(*) INTO cnt_pos FROM stock_position;
    SELECT count(*) INTO cnt_lot FROM position_lot;
    SELECT count(*) INTO cnt_disp FROM lot_disposal;
    RAISE NOTICE '[demo-data] complete — users=%, admins=%, stocks=%, accounts=%, txs=%, positions=%, lots=%, disposals=%',
        cnt_users, cnt_admins, cnt_stocks, cnt_acct, cnt_tx, cnt_pos, cnt_lot, cnt_disp;
END $$;
