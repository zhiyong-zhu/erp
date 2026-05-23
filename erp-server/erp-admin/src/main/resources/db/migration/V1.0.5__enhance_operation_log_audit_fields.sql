ALTER TABLE sys_operation_log
    ADD COLUMN IF NOT EXISTS trace_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS success BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS error_message VARCHAR(500),
    ADD COLUMN IF NOT EXISTS data_scope_level VARCHAR(50),
    ADD COLUMN IF NOT EXISTS data_scope_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS field_permission_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS audit_tags TEXT;

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_trace_id ON sys_operation_log (trace_id);
