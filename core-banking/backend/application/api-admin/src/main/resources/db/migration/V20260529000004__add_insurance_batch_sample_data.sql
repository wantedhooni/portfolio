-- ============================================================
--  보험 배치 샘플 데이터
--
--  목적: InsurancePremiumSettlementJob / InsuranceLedgerPostingBatchJob 테스트
--
--  데이터 구성:
--    1. insurance_product  — 보험 상품 10종
--    2. insurance_policy   — ACTIVE 계약 500건
--       · next_payment_date = 오늘(짝수) / 어제(홀수) → 자동이체 배치 대상
--    3. premium_payment    — 과거 5개월 PAID 납부 (~2,500건) + 오늘 PENDING (~500건)
--       · PAID  건: 원장 전기 배치(InsuranceLedgerPostingBatchJob) 대상
--       · PENDING 건: 자동이체 정산 배치(InsurancePremiumSettlementJob) 대상
--
--  멱등성: 모든 INSERT 에 ON CONFLICT DO NOTHING 적용
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. 보험 상품 (10종)
-- ─────────────────────────────────────────────────────────────
INSERT INTO insurance_product (created_at, updated_at, product_code, name, description,
                               insurance_type, base_premium, premium_frequency,
                               coverage_amount, duration_months, currency, is_active)
VALUES
    (now(), now(), 'LIFE-TERM-01',  '정기사망보험 기본형',  '사망 시 보험금 지급',         'LIFE',       150000, 'MONTHLY',  100000000, 240, 'KRW', true),
    (now(), now(), 'LIFE-TERM-02',  '정기사망보험 고급형',  '사망 시 보험금 지급 (고액)',   'LIFE',       350000, 'MONTHLY',  300000000, 240, 'KRW', true),
    (now(), now(), 'HEALTH-STD-01', '실손의료보험 표준형',  '의료비 실손 보상',             'HEALTH',      80000, 'MONTHLY',   50000000, 120, 'KRW', true),
    (now(), now(), 'HEALTH-PRE-01', '실손의료보험 프리미엄','의료비 실손 보상 (특약 포함)', 'HEALTH',     130000, 'MONTHLY',   80000000, 120, 'KRW', true),
    (now(), now(), 'ACCIDENT-01',   '상해보험 기본형',      '상해 사고 시 보장',            'ACCIDENT',    55000, 'MONTHLY',   30000000,  60, 'KRW', true),
    (now(), now(), 'ACCIDENT-02',   '상해보험 플러스형',    '상해 + 입원 보장',             'ACCIDENT',    90000, 'MONTHLY',   50000000,  60, 'KRW', true),
    (now(), now(), 'ANNUITY-01',    '연금보험 10년납',      '10년 납입 후 연금 지급',       'ANNUITY',    300000, 'MONTHLY',  200000000, 120, 'KRW', true),
    (now(), now(), 'ANNUITY-02',    '연금보험 20년납',      '20년 납입 후 연금 지급',       'ANNUITY',    200000, 'MONTHLY',  300000000, 240, 'KRW', true),
    (now(), now(), 'SAVINGS-01',    '저축보험 5년납',       '5년 만기 적립형',              'SAVINGS',    100000, 'MONTHLY',   70000000,  60, 'KRW', true),
    (now(), now(), 'DISABILITY-01', '장기장해보험',         '장해 발생 시 연금 지급',       'DISABILITY', 120000, 'MONTHLY',  120000000, 180, 'KRW', true)
ON CONFLICT (product_code) DO NOTHING;


-- ─────────────────────────────────────────────────────────────
-- 2. 보험 계약 (500건)
--    · 사용자 100명 × 평균 5계약
--    · 상품 10종 순환 배정
--    · next_payment_date = 오늘(짝수 n) / 어제(홀수 n) — 배치 대상
-- ─────────────────────────────────────────────────────────────
WITH
user_pool    AS (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM users WHERE role = 'USER'),
product_pool AS (SELECT id, base_premium, coverage_amount, duration_months,
                        ROW_NUMBER() OVER (ORDER BY id) AS rn
                 FROM insurance_product WHERE is_active = true),
account_pool AS (SELECT id, user_id, COUNT(*) OVER () AS cnt,
                        ROW_NUMBER() OVER (ORDER BY id) AS rn
                 FROM account)
INSERT INTO insurance_policy (
    created_at, updated_at, version,
    policy_number, product_id, user_id, insured_user_id, billing_account_id,
    premium, premium_frequency, coverage_amount, currency,
    start_date, end_date, next_payment_date, status, activated_at, terminated_at
)
SELECT
    now() - (((n % 24) + 1) || ' months')::interval,
    now() - (((n % 24) + 1) || ' months')::interval,
    0,
    'POL-' || lpad(n::text, 6, '0'),
    (SELECT id FROM product_pool WHERE rn = (n % 10) + 1),
    (SELECT id FROM user_pool    WHERE rn = (n % 100) + 1),
    (SELECT id FROM user_pool    WHERE rn = (n % 100) + 1),
    COALESCE(
        (SELECT id FROM account_pool WHERE rn = (n % cnt) + 1),
        (SELECT id FROM account ORDER BY id LIMIT 1)
    ),
    (SELECT base_premium     FROM product_pool WHERE rn = (n % 10) + 1),
    'MONTHLY',
    (SELECT coverage_amount  FROM product_pool WHERE rn = (n % 10) + 1),
    'KRW',
    (now() - (((n % 24) + 1) || ' months')::interval)::date,
    (now() - (((n % 24) + 1) || ' months')::interval
     + ((SELECT duration_months FROM product_pool WHERE rn = (n % 10) + 1) || ' months')::interval)::date,
    CASE WHEN n % 2 = 0 THEN CURRENT_DATE ELSE CURRENT_DATE - 1 END,
    'ACTIVE',
    now() - (((n % 24) + 1) || ' months')::interval,
    NULL
FROM generate_series(1, 500) AS s(n)
CROSS JOIN (SELECT COUNT(*) AS cnt FROM account_pool) ac
WHERE (SELECT COUNT(*) FROM user_pool)    >= 1
  AND (SELECT COUNT(*) FROM product_pool) >= 1
  AND (SELECT COUNT(*) FROM account_pool) >= 1
  AND NOT EXISTS (
      SELECT 1 FROM insurance_policy ip
      WHERE ip.policy_number = 'POL-' || lpad(s.n::text, 6, '0')
  );


-- ─────────────────────────────────────────────────────────────
-- 3-A. 보험료 납부 내역 — 과거 5개월 PAID (원장 전기 배치 대상)
--      · policy 500건 × 5개월 = ~2,500건
--      · reference_id = 'PREM-{policy_id}-{yyyy-MM-dd}'
-- ─────────────────────────────────────────────────────────────
INSERT INTO premium_payment (
    created_at, updated_at,
    policy_id, amount, currency,
    due_date, paid_at, billing_account_id, account_tx_id,
    status, reference_id, failure_reason
)
SELECT
    pol.activated_at + ((m - 1) || ' months')::interval,
    pol.activated_at + ((m - 1) || ' months')::interval + interval '1 hour',
    pol.id,
    pol.premium,
    pol.currency,
    (pol.start_date + ((m - 1) || ' months')::interval)::date,
    pol.activated_at + ((m - 1) || ' months')::interval + interval '2 hours',
    pol.billing_account_id,
    NULL,
    'PAID',
    'PREM-' || pol.id || '-' || (pol.start_date + ((m - 1) || ' months')::interval)::date,
    NULL
FROM insurance_policy pol
CROSS JOIN generate_series(1, 5) AS s(m)
WHERE pol.policy_number LIKE 'POL-%'
  AND pol.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM premium_payment pp
      WHERE pp.reference_id = 'PREM-' || pol.id || '-' ||
            (pol.start_date + ((s.m - 1) || ' months')::interval)::date
  );


-- ─────────────────────────────────────────────────────────────
-- 3-B. 보험료 납부 내역 — 오늘/어제 PENDING (자동이체 배치 대상)
-- ─────────────────────────────────────────────────────────────
INSERT INTO premium_payment (
    created_at, updated_at,
    policy_id, amount, currency,
    due_date, paid_at, billing_account_id, account_tx_id,
    status, reference_id, failure_reason
)
SELECT
    now() - interval '10 minutes',
    now() - interval '10 minutes',
    pol.id,
    pol.premium,
    pol.currency,
    pol.next_payment_date,
    NULL,
    pol.billing_account_id,
    NULL,
    'PENDING',
    'PREM-' || pol.id || '-' || pol.next_payment_date,
    NULL
FROM insurance_policy pol
WHERE pol.policy_number LIKE 'POL-%'
  AND pol.status = 'ACTIVE'
  AND pol.next_payment_date IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM premium_payment pp
      WHERE pp.reference_id = 'PREM-' || pol.id || '-' || pol.next_payment_date
  );


-- ─────────────────────────────────────────────────────────────
-- 확인
-- ─────────────────────────────────────────────────────────────
DO $$
DECLARE
    v_product  INT;
    v_policy   INT;
    v_paid     INT;
    v_pending  INT;
BEGIN
    SELECT COUNT(*) INTO v_product FROM insurance_product;
    SELECT COUNT(*) INTO v_policy  FROM insurance_policy  WHERE policy_number LIKE 'POL-%';
    SELECT COUNT(*) INTO v_paid    FROM premium_payment   WHERE reference_id LIKE 'PREM-%' AND status = 'PAID';
    SELECT COUNT(*) INTO v_pending FROM premium_payment   WHERE reference_id LIKE 'PREM-%' AND status = 'PENDING';
    RAISE NOTICE '[보험 배치 샘플] 상품=% 계약=% PAID납부=% PENDING납부=%',
        v_product, v_policy, v_paid, v_pending;
END $$;
