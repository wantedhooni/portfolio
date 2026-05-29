-- ============================================================
--  PG 도메인 샘플 데이터
--
--  목적: PgSettlementBatchJob (가맹점별 일별 정산 + 원장 전기) 테스트
--
--  데이터 구성:
--    1. pg_merchant — 실제 업종 가맹점 20개
--    2. pg_payment  — APPROVED 결제 6,000건 (최근 30일 분산)
--       · status = APPROVED, pg_settlement_id = NULL → 배치 정산 대상
--       · 결제 수단 분산: CARD 50% / KAKAO_PAY 20% / NAVER_PAY 15% / etc.
--
--  멱등성: merchant_code / order_no 중복 방지
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. PG 가맹점 (20개)
--    settlement_account_id = 기존 account 테이블 순환 참조
-- ─────────────────────────────────────────────────────────────
WITH account_pool AS (
    SELECT id, COUNT(*) OVER () AS cnt, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM account
)
INSERT INTO pg_merchant (
    created_at, updated_at,
    merchant_code, name, business_type,
    settlement_account_id,
    commission_rate, settlement_cycle,
    currency, is_active, contact_email
)
SELECT
    now() - ((v.n * 3) || ' days')::interval,
    now() - ((v.n * 3) || ' days')::interval,
    'MER-' || lpad(v.n::text, 4, '0'),
    v.merchant_name,
    v.biz_type,
    COALESCE(
        (SELECT id FROM account_pool WHERE rn = ((v.n - 1) % cnt) + 1),
        (SELECT id FROM account ORDER BY id LIMIT 1)
    ),
    v.rate,
    v.cycle,
    'KRW',
    true,
    'merchant' || v.n || '@example.com'
FROM (VALUES
    (1,  '스타벅스코리아',   'FOOD',       0.0180, 2),
    (2,  '쿠팡',             'RETAIL',     0.0250, 2),
    (3,  '네이버쇼핑',       'RETAIL',     0.0200, 2),
    (4,  '배달의민족',       'FOOD',       0.0300, 2),
    (5,  '야놀자',           'TRAVEL',     0.0280, 3),
    (6,  '마켓컬리',         'FOOD',       0.0220, 2),
    (7,  'SSG닷컴',          'RETAIL',     0.0230, 2),
    (8,  '롯데온',           'RETAIL',     0.0210, 2),
    (9,  '올리브영',         'HEALTHCARE', 0.0190, 2),
    (10, '무신사',           'RETAIL',     0.0240, 2),
    (11, '교보문고',         'DIGITAL',    0.0150, 2),
    (12, '왓챠',             'DIGITAL',    0.0350, 3),
    (13, '넷플릭스코리아',   'DIGITAL',    0.0320, 3),
    (14, '에어비앤비코리아', 'TRAVEL',     0.0300, 3),
    (15, '카카오택시',       'TRANSPORT',  0.0200, 2),
    (16, '토스',             'FINTECH',    0.0100, 1),
    (17, '당근마켓',         'RETAIL',     0.0180, 2),
    (18, '원스토어',         'DIGITAL',    0.0300, 3),
    (19, '인터파크',         'TRAVEL',     0.0260, 2),
    (20, '지마켓',           'RETAIL',     0.0220, 2)
) AS v(n, merchant_name, biz_type, rate, cycle)
WHERE EXISTS (SELECT 1 FROM account)
  AND NOT EXISTS (
      SELECT 1 FROM pg_merchant
      WHERE merchant_code = 'MER-' || lpad(v.n::text, 4, '0')
  );


-- ─────────────────────────────────────────────────────────────
-- 2. PG 결제 (~6,000건, 최근 30일)
--    · 가맹점 20개 순환 배정
--    · 결제 수단 분산 (20개 버킷):
--        CARD          0~9  (50%)
--        KAKAO_PAY     10~13 (20%)
--        NAVER_PAY     14~16 (15%)
--        BANK_TRANSFER 17~18 (10%)
--        TOSS_PAY      19   (5%)
--    · 금액: ((n % 100) + 1) × 5,000원  →  5,000 ~ 500,000원
--    · commission = amount × commission_rate (소수점 2자리 반올림)
--    · pg_settlement_id = NULL (정산 배치 대상)
-- ─────────────────────────────────────────────────────────────
WITH merchant_pool AS (
    SELECT id, commission_rate,
           COUNT(*) OVER () AS cnt,
           ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM pg_merchant WHERE is_active = true
)
INSERT INTO pg_payment (
    created_at, updated_at,
    merchant_id, payment_method,
    order_no, amount, commission_amount, net_amount,
    currency, status, requested_at, approved_at, cancelled_at, pg_settlement_id
)
SELECT
    now() - ((n % 30)   || ' days')::interval
            - ((n % 1440) || ' minutes')::interval,
    now() - ((n % 30)   || ' days')::interval
            - ((n % 1440) || ' minutes')::interval + interval '2 seconds',
    -- 가맹점 순환
    (SELECT id FROM merchant_pool WHERE rn = (n % (SELECT cnt FROM merchant_pool LIMIT 1)) + 1),
    -- 결제 수단
    CASE (n % 20)
        WHEN 10 THEN 'KAKAO_PAY'
        WHEN 11 THEN 'KAKAO_PAY'
        WHEN 12 THEN 'KAKAO_PAY'
        WHEN 13 THEN 'KAKAO_PAY'
        WHEN 14 THEN 'NAVER_PAY'
        WHEN 15 THEN 'NAVER_PAY'
        WHEN 16 THEN 'NAVER_PAY'
        WHEN 17 THEN 'BANK_TRANSFER'
        WHEN 18 THEN 'BANK_TRANSFER'
        WHEN 19 THEN 'TOSS_PAY'
        ELSE         'CARD'
    END,
    'PG-ORD-' || lpad(n::text, 8, '0'),
    -- amount
    (((n % 100) + 1) * 5000)::NUMERIC(20, 2),
    -- commission_amount
    ROUND(
        (((n % 100) + 1) * 5000) *
        (SELECT commission_rate FROM merchant_pool WHERE rn = (n % (SELECT cnt FROM merchant_pool LIMIT 1)) + 1),
        2
    )::NUMERIC(20, 2),
    -- net_amount
    ROUND(
        (((n % 100) + 1) * 5000) *
        (1 - (SELECT commission_rate FROM merchant_pool WHERE rn = (n % (SELECT cnt FROM merchant_pool LIMIT 1)) + 1)),
        2
    )::NUMERIC(20, 2),
    'KRW',
    'APPROVED',
    now() - ((n % 30) || ' days')::interval - ((n % 1440) || ' minutes')::interval,
    now() - ((n % 30) || ' days')::interval - ((n % 1440) || ' minutes')::interval + interval '2 seconds',
    NULL,
    NULL
FROM generate_series(1, 6000) AS s(n)
WHERE EXISTS (SELECT 1 FROM merchant_pool)
  AND NOT EXISTS (
      SELECT 1 FROM pg_payment pp
      WHERE pp.order_no = 'PG-ORD-' || lpad(s.n::text, 8, '0')
  );


-- ─────────────────────────────────────────────────────────────
-- 확인
-- ─────────────────────────────────────────────────────────────
DO $$
DECLARE
    v_merchant  INT;
    v_payment   INT;
BEGIN
    SELECT COUNT(*) INTO v_merchant FROM pg_merchant WHERE merchant_code LIKE 'MER-%';
    SELECT COUNT(*) INTO v_payment  FROM pg_payment  WHERE order_no      LIKE 'PG-ORD-%' AND status = 'APPROVED';
    RAISE NOTICE '[PG 샘플] 가맹점=% APPROVED결제=%', v_merchant, v_payment;
END $$;
