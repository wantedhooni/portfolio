CREATE TABLE QRTZ_JOB_EXECUTION_HISTORY (
                                              id BIGSERIAL PRIMARY KEY,

                                              scheduler_name VARCHAR(120),
                                              fire_instance_id VARCHAR(200) NOT NULL,

                                              job_name VARCHAR(200) NOT NULL,
                                              job_group VARCHAR(200) NOT NULL,

                                              trigger_name VARCHAR(200),
                                              trigger_group VARCHAR(200),

                                              status VARCHAR(30) NOT NULL,

                                              scheduled_fire_time TIMESTAMP,
                                              fire_time TIMESTAMP NOT NULL,
                                              end_time TIMESTAMP,

                                              duration_ms BIGINT,

                                              refire_count INTEGER,
                                              recovering BOOLEAN,

                                              error_message TEXT,
                                              error_stack_trace TEXT,

                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_qrtz_job_history_fire_instance
    ON qrtz_job_execution_history (fire_instance_id);

CREATE INDEX idx_qrtz_job_history_job
    ON qrtz_job_execution_history (job_group, job_name);

CREATE INDEX idx_qrtz_job_history_status
    ON qrtz_job_execution_history (status);

CREATE INDEX idx_qrtz_job_history_fire_time
    ON qrtz_job_execution_history (fire_time DESC);