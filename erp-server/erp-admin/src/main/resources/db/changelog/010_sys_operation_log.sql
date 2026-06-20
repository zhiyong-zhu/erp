--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-3-add-dict-and-operation-log-003
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGSERIAL,
    user_id UUID,
    username VARCHAR(50),
    module VARCHAR(50),
    action VARCHAR(50),
    description VARCHAR(500),
    method VARCHAR(200),
    request_url VARCHAR(500),
    request_params TEXT,
    response_code INTEGER,
    ip VARCHAR(50),
    duration INTEGER,
    created_at TIMESTAMPTZ DEFAULT now()
) PARTITION BY RANGE (created_at);

--changeset erp:v1-0-3-add-dict-and-operation-log-004
CREATE TABLE IF NOT EXISTS sys_operation_log_default PARTITION OF sys_operation_log DEFAULT;

--changeset erp:v1-0-3-add-dict-and-operation-log-005 splitStatements:false
DO $$
DECLARE
    start_ts TIMESTAMPTZ := date_trunc('month', now());
    next_ts TIMESTAMPTZ := start_ts + INTERVAL '1 month';
    partition_name TEXT := format('sys_operation_log_%s', to_char(start_ts, 'YYYYMM'));
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF sys_operation_log FOR VALUES FROM (%L) TO (%L)',
        partition_name,
        start_ts,
        next_ts
    );
END $$;

--changeset erp:v1-0-3-add-dict-and-operation-log-006
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_created_at ON sys_operation_log (created_at DESC);

--changeset erp:v1-0-3-add-dict-and-operation-log-007
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_user_id ON sys_operation_log (user_id);

--changeset erp:v1-0-3-add-dict-and-operation-log-008
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_module ON sys_operation_log (module);

--changeset erp:v1-0-5-enhance-operation-log-audit-fields-001
ALTER TABLE sys_operation_log
    ADD COLUMN IF NOT EXISTS trace_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS success BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS error_message VARCHAR(500),
    ADD COLUMN IF NOT EXISTS data_scope_level VARCHAR(50),
    ADD COLUMN IF NOT EXISTS data_scope_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS field_permission_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS audit_tags TEXT;

--changeset erp:v1-0-5-enhance-operation-log-audit-fields-002
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_trace_id ON sys_operation_log (trace_id);
