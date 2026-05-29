-- ============================================================
--  원장 마스터 데이터
--
--  1. 계정과목 (ledger_account) — 배치 원장 전기에 필요한 계정
--  2. 회계기간 (accounting_period) — 2026년 1 ~ 12월
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. 계정과목 (Chart of Accounts)
--
--  계층 구조:
--    그룹 계정 (parent_id NULL)
--      1000 자산총계 → 1001 현금및현금성자산, 1002 증권투자자산
--      2000 부채총계 → 2001 미지급금, 2002 가맹점정산부채
--      3000 자본총계 → 3001 자본금
--      4000 수익총계 → 4001 보험료수입, 4002 수수료수입, 4003 배당수입, 4004 매매손익
--      5000 비용총계 → 5001 매매비용, 5002 보험금비용
--
--  배치별 사용 계정:
--    보험료 원장 전기     : DR 1001 / CR 4001
--    증권 매수 원장       : DR 1002 + DR 5001 / CR 1001
--    증권 매도 원장       : DR 1001 + DR 5001 / CR 1002
--    PG 정산 원장 전기    : DR 1001 / CR 2002 + CR 4002
-- ─────────────────────────────────────────────────────────────

-- 그룹(부모) 계정
INSERT INTO ledger_account (created_at, updated_at, account_code, name, category, normal_balance,
                            parent_id, currency, is_active, description)
VALUES
    (now(), now(), '1000', '자산총계',   'ASSET',    'DEBIT',  NULL, 'KRW', true, '자산 그룹 계정'),
    (now(), now(), '2000', '부채총계',   'LIABILITY','CREDIT', NULL, 'KRW', true, '부채 그룹 계정'),
    (now(), now(), '3000', '자본총계',   'EQUITY',  'CREDIT',  NULL, 'KRW', true, '자본 그룹 계정'),
    (now(), now(), '4000', '수익총계',   'REVENUE', 'CREDIT',  NULL, 'KRW', true, '수익 그룹 계정'),
    (now(), now(), '5000', '비용총계',   'EXPENSE', 'DEBIT',   NULL, 'KRW', true, '비용 그룹 계정')
ON CONFLICT (account_code) DO NOTHING;

-- 자식 계정
INSERT INTO ledger_account (created_at, updated_at, account_code, name, category, normal_balance,
                            parent_id, currency, is_active, description)
SELECT now(), now(), v.code, v.name, v.category,
       v.normal_balance,
       (SELECT id FROM ledger_account WHERE account_code = v.parent_code),
       'KRW', true, v.description
FROM (VALUES
    -- 자산
    ('1001', '현금및현금성자산', 'ASSET',    'DEBIT',  '1000', '현금·보통예금·요구불예금 — 배치 수령·지급 대변'),
    ('1002', '증권투자자산',    'ASSET',    'DEBIT',  '1000', '주식·채권 등 유가증권 취득원가'),
    -- 부채
    ('2001', '미지급금',        'LIABILITY','CREDIT', '2000', '미지급 수수료·세금'),
    ('2002', '가맹점정산부채',  'LIABILITY','CREDIT', '2000', 'PG 정산 가맹점 지급 예정액'),
    -- 자본
    ('3001', '자본금',          'EQUITY',  'CREDIT',  '3000', '납입자본'),
    -- 수익
    ('4001', '보험료수입',      'REVENUE', 'CREDIT',  '4000', '보험계약에서 발생한 수입보험료'),
    ('4002', '수수료수입',      'REVENUE', 'CREDIT',  '4000', '거래 수수료 수입 (PG 수수료 포함)'),
    ('4003', '배당수입',        'REVENUE', 'CREDIT',  '4000', '주식 배당금 수입'),
    ('4004', '매매손익',        'REVENUE', 'CREDIT',  '4000', '유가증권 처분손익'),
    -- 비용
    ('5001', '매매비용',        'EXPENSE', 'DEBIT',   '5000', '주식 거래 수수료 및 증권거래세'),
    ('5002', '보험금비용',      'EXPENSE', 'DEBIT',   '5000', '보험사고 지급 보험금')
) AS v(code, name, category, normal_balance, parent_code, description)
WHERE (SELECT id FROM ledger_account WHERE account_code = v.parent_code) IS NOT NULL
ON CONFLICT (account_code) DO NOTHING;


-- ─────────────────────────────────────────────────────────────
-- 2. 회계기간 (2026년 1 ~ 12월)
--
--    1 ~ 4월  : CLOSED (마감)
--    5 ~ 12월 : OPEN   (분개 입력 가능)
--    (실행 시점 기준이 2026-05-29 이므로 현재 기간은 5월)
-- ─────────────────────────────────────────────────────────────
INSERT INTO accounting_period (created_at, updated_at, fiscal_year, fiscal_period,
                               start_date, end_date, status, closed_at, closed_by_admin_id)
SELECT
    now() - ((13 - m) || ' months')::interval,
    now() - ((13 - m) || ' months')::interval,
    2026,
    m,
    make_date(2026, m, 1),
    (make_date(2026, m, 1) + interval '1 month - 1 day')::date,
    CASE WHEN m < 5 THEN 'CLOSED' ELSE 'OPEN' END,
    CASE WHEN m < 5
         THEN (make_date(2026, m, 1) + interval '1 month - 1 day' + interval '2 days')
         ELSE NULL END,
    CASE WHEN m < 5
         THEN (SELECT id FROM admin ORDER BY id LIMIT 1)
         ELSE NULL END
FROM generate_series(1, 12) AS s(m)
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_period ap
    WHERE ap.fiscal_year = 2026 AND ap.fiscal_period = s.m
);
