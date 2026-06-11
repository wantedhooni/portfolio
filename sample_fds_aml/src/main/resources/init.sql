-- V1__create_fds_schema.sql
CREATE SCHEMA IF NOT EXISTS fds;

CREATE TABLE fds.transactions (
                                  transaction_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  account_id      UUID NOT NULL,
                                  counterpart_account_id UUID,
                                  transaction_type VARCHAR(30) NOT NULL,
                                  amount          NUMERIC(20, 2) NOT NULL,
                                  currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
                                  status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                  channel         VARCHAR(20),
                                  country_code    VARCHAR(2),
                                  city            VARCHAR(100),
                                  latitude        DOUBLE PRECISION,
                                  longitude       DOUBLE PRECISION,
                                  device_id       VARCHAR(100),
                                  device_type     VARCHAR(30),
                                  ip_address      VARCHAR(45),
                                  user_agent      VARCHAR(500),
                                  description     VARCHAR(200),
                                  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMP,
                                  created_by      VARCHAR(50),
                                  updated_by      VARCHAR(50)
);

CREATE INDEX idx_txn_account_id    ON fds.transactions(account_id);
CREATE INDEX idx_txn_created_at    ON fds.transactions(created_at DESC);
CREATE INDEX idx_txn_status        ON fds.transactions(status);
CREATE INDEX idx_txn_type_created  ON fds.transactions(transaction_type, created_at DESC);

-- 파티션 전략 (월별 - 대용량 거래 대비)
-- 실 운영에서는 파티셔닝 필수
CREATE TABLE fds.fds_alerts (
                                alert_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                transaction_id  UUID NOT NULL,
                                account_id      UUID NOT NULL,
                                rule_code       VARCHAR(50) NOT NULL,
                                severity        VARCHAR(20) NOT NULL,
                                status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                                score           INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
                                evidence        JSONB,
                                analyst_note    VARCHAR(500),
                                resolved_at     TIMESTAMP,
                                resolved_by     VARCHAR(50),
                                created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at      TIMESTAMP,
                                created_by      VARCHAR(50),
                                updated_by      VARCHAR(50)
);

CREATE INDEX idx_alert_txn_id   ON fds.fds_alerts(transaction_id);
CREATE INDEX idx_alert_status    ON fds.fds_alerts(status);
CREATE INDEX idx_alert_severity  ON fds.fds_alerts(severity);
CREATE INDEX idx_alert_created   ON fds.fds_alerts(created_at DESC);
-- JSONB 인덱스 (evidence 필드 검색용)
CREATE INDEX idx_alert_evidence  ON fds.fds_alerts USING gin(evidence);

CREATE TABLE fds.aml_cases (
                               case_id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               customer_id               UUID NOT NULL,
                               case_type                 VARCHAR(10) NOT NULL,
                               status                    VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                               detection_date            DATE NOT NULL,
                               report_deadline           DATE,
                               related_transaction_ids   JSONB,
                               total_amount              NUMERIC(20, 2),
                               fiu_report_id             VARCHAR(50),
                               filed_at                  TIMESTAMP,
                               investigator              VARCHAR(50),
                               summary                   VARCHAR(2000),
                               created_at                TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at                TIMESTAMP,
                               created_by                VARCHAR(50),
                               updated_by                VARCHAR(50)
);

CREATE INDEX idx_aml_customer_id       ON fds.aml_cases(customer_id);
CREATE INDEX idx_aml_case_type_status  ON fds.aml_cases(case_type, status);
CREATE INDEX idx_aml_deadline          ON fds.aml_cases(report_deadline);