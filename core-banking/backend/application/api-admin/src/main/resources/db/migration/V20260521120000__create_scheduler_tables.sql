-- =====================================================================
-- Quartz Scheduler (PostgreSQL) - 공식 배포 DDL (v2.5.x) 기반
-- =====================================================================

CREATE TABLE qrtz_job_details (
    sched_name        VARCHAR(120) NOT NULL,
    job_name          VARCHAR(200) NOT NULL,
    job_group         VARCHAR(200) NOT NULL,
    description       VARCHAR(250) NULL,
    job_class_name    VARCHAR(250) NOT NULL,
    is_durable        BOOL NOT NULL,
    is_nonconcurrent  BOOL NOT NULL,
    is_update_data    BOOL NOT NULL,
    requests_recovery BOOL NOT NULL,
    job_data          BYTEA NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE TABLE qrtz_triggers (
    sched_name     VARCHAR(120) NOT NULL,
    trigger_name   VARCHAR(200) NOT NULL,
    trigger_group  VARCHAR(200) NOT NULL,
    job_name       VARCHAR(200) NOT NULL,
    job_group      VARCHAR(200) NOT NULL,
    description    VARCHAR(250) NULL,
    next_fire_time BIGINT NULL,
    prev_fire_time BIGINT NULL,
    priority       INTEGER NULL,
    trigger_state  VARCHAR(16) NOT NULL,
    trigger_type   VARCHAR(8) NOT NULL,
    start_time     BIGINT NOT NULL,
    end_time       BIGINT NULL,
    calendar_name  VARCHAR(200) NULL,
    misfire_instr  SMALLINT NULL,
    job_data       BYTEA NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, job_name, job_group)
        REFERENCES qrtz_job_details (sched_name, job_name, job_group)
);

CREATE TABLE qrtz_simple_triggers (
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    repeat_count    BIGINT NOT NULL,
    repeat_interval BIGINT NOT NULL,
    times_triggered BIGINT NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE qrtz_cron_triggers (
    sched_name      VARCHAR(120) NOT NULL,
    trigger_name    VARCHAR(200) NOT NULL,
    trigger_group   VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(120) NOT NULL,
    time_zone_id    VARCHAR(80),
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE qrtz_simprop_triggers (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    str_prop_1    VARCHAR(512) NULL,
    str_prop_2    VARCHAR(512) NULL,
    str_prop_3    VARCHAR(512) NULL,
    int_prop_1    INTEGER NULL,
    int_prop_2    INTEGER NULL,
    long_prop_1   BIGINT NULL,
    long_prop_2   BIGINT NULL,
    dec_prop_1    NUMERIC(13,4) NULL,
    dec_prop_2    NUMERIC(13,4) NULL,
    bool_prop_1   BOOL NULL,
    bool_prop_2   BOOL NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE qrtz_blob_triggers (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_name  VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data     BYTEA NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
);

CREATE TABLE qrtz_calendars (
    sched_name    VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar      BYTEA NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

CREATE TABLE qrtz_paused_trigger_grps (
    sched_name    VARCHAR(120) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

CREATE TABLE qrtz_fired_triggers (
    sched_name        VARCHAR(120) NOT NULL,
    entry_id          VARCHAR(95) NOT NULL,
    trigger_name      VARCHAR(200) NOT NULL,
    trigger_group     VARCHAR(200) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    fired_time        BIGINT NOT NULL,
    sched_time        BIGINT NOT NULL,
    priority          INTEGER NOT NULL,
    state             VARCHAR(16) NOT NULL,
    job_name          VARCHAR(200) NULL,
    job_group         VARCHAR(200) NULL,
    is_nonconcurrent  BOOL NULL,
    requests_recovery BOOL NULL,
    PRIMARY KEY (sched_name, entry_id)
);

CREATE TABLE qrtz_scheduler_state (
    sched_name        VARCHAR(120) NOT NULL,
    instance_name     VARCHAR(200) NOT NULL,
    last_checkin_time BIGINT NOT NULL,
    checkin_interval  BIGINT NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

CREATE TABLE qrtz_locks (
    sched_name VARCHAR(120) NOT NULL,
    lock_name  VARCHAR(40) NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details (sched_name, requests_recovery);
CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers (sched_name, next_fire_time);
CREATE INDEX idx_qrtz_t_state ON qrtz_triggers (sched_name, trigger_state);
CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers (sched_name, trigger_state, next_fire_time);
CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers (sched_name, instance_name);
CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers (sched_name, trigger_name, trigger_group);

-- =====================================================================
-- Quartz Job 실행 이력 (사용자 정의 - 워커 노드의 JobListener 가 기록)
-- =====================================================================

CREATE TABLE qrtz_job_history (
    id                BIGSERIAL PRIMARY KEY,
    instance_id       VARCHAR(200) NOT NULL,
    job_name          VARCHAR(200) NOT NULL,
    job_group         VARCHAR(200) NOT NULL,
    trigger_name      VARCHAR(200) NOT NULL,
    trigger_group     VARCHAR(200) NOT NULL,
    fired_at          TIMESTAMP NOT NULL,
    completed_at      TIMESTAMP NULL,
    run_time_ms       BIGINT NULL,
    result            VARCHAR(20) NOT NULL,   -- SUCCESS / FAILED / VETOED
    exception_message TEXT NULL,
    job_data          TEXT NULL
);
CREATE INDEX idx_qrtz_jh_job ON qrtz_job_history (job_name, job_group, fired_at DESC);
CREATE INDEX idx_qrtz_jh_fired ON qrtz_job_history (fired_at DESC);

-- =====================================================================
-- Spring Batch 5.x 메타 테이블 (PostgreSQL)
-- =====================================================================

CREATE TABLE batch_job_instance (
    job_instance_id BIGINT PRIMARY KEY,
    version         BIGINT,
    job_name        VARCHAR(100) NOT NULL,
    job_key         VARCHAR(32)  NOT NULL,
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE batch_job_execution (
    job_execution_id BIGINT PRIMARY KEY,
    version          BIGINT,
    job_instance_id  BIGINT NOT NULL,
    create_time      TIMESTAMP NOT NULL,
    start_time       TIMESTAMP DEFAULT NULL,
    end_time         TIMESTAMP DEFAULT NULL,
    status           VARCHAR(10),
    exit_code        VARCHAR(2500),
    exit_message     VARCHAR(2500),
    last_updated     TIMESTAMP,
    CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
        REFERENCES batch_job_instance (job_instance_id)
);

CREATE TABLE batch_job_execution_params (
    job_execution_id BIGINT NOT NULL,
    parameter_name   VARCHAR(100) NOT NULL,
    parameter_type   VARCHAR(100) NOT NULL,
    parameter_value  VARCHAR(2500),
    identifying      CHAR(1) NOT NULL,
    CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution (job_execution_id)
);

CREATE TABLE batch_step_execution (
    step_execution_id  BIGINT PRIMARY KEY,
    version            BIGINT NOT NULL,
    step_name          VARCHAR(100) NOT NULL,
    job_execution_id   BIGINT NOT NULL,
    create_time        TIMESTAMP NOT NULL,
    start_time         TIMESTAMP DEFAULT NULL,
    end_time           TIMESTAMP DEFAULT NULL,
    status             VARCHAR(10),
    commit_count       BIGINT,
    read_count         BIGINT,
    filter_count       BIGINT,
    write_count        BIGINT,
    read_skip_count    BIGINT,
    write_skip_count   BIGINT,
    process_skip_count BIGINT,
    rollback_count     BIGINT,
    exit_code          VARCHAR(2500),
    exit_message       VARCHAR(2500),
    last_updated       TIMESTAMP,
    CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution (job_execution_id)
);

CREATE TABLE batch_step_execution_context (
    step_execution_id  BIGINT PRIMARY KEY,
    short_context      VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
        REFERENCES batch_step_execution (step_execution_id)
);

CREATE TABLE batch_job_execution_context (
    job_execution_id   BIGINT PRIMARY KEY,
    short_context      VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution (job_execution_id)
);

CREATE SEQUENCE batch_step_execution_seq    MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE batch_job_execution_seq     MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE batch_job_seq               MAXVALUE 9223372036854775807 NO CYCLE;

-- =====================================================================
-- Batch 실행 요청 큐 (워커 노드가 폴링하여 launch 수행) - 선택 사용
-- =====================================================================

CREATE TABLE batch_launch_request (
    id              BIGSERIAL PRIMARY KEY,
    job_name        VARCHAR(100) NOT NULL,
    job_parameters  TEXT,
    requested_by    VARCHAR(100),
    requested_at    TIMESTAMP NOT NULL DEFAULT now(),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / PICKED / DONE / FAILED
    picked_at       TIMESTAMP NULL,
    picked_by       VARCHAR(200) NULL,
    execution_id    BIGINT NULL,
    error_message   TEXT NULL
);
CREATE INDEX idx_batch_launch_status ON batch_launch_request (status, requested_at);
