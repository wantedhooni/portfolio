-- ============================================================
--  journal_entry.journal_number 컬럼 길이 확장 (30 → 64)
--
--  변경 이유:
--    배치 자동 전기 시 생성하는 분개번호 형식:
--      · 보험료: "JRNL-PREM-PREM-{policyId}-{date}"  → 최대 ~50자
--      · 증권:   "JRNL-TRADE-{referenceId}"          → 최대 ~50자
--      · PG:     "JRNL-PG-PGSTL-{merchantId}-{date}" → 최대 ~45자
--    기존 varchar(30) 으로는 저장 불가. varchar(64) 로 확장.
-- ============================================================

ALTER TABLE journal_entry
    ALTER COLUMN journal_number TYPE VARCHAR(64);
