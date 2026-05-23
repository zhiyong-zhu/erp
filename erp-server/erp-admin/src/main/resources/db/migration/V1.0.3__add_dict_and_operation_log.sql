CREATE TABLE IF NOT EXISTS sys_dict_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dict_type_code VARCHAR(100) NOT NULL REFERENCES sys_dict_type(code) ON UPDATE CASCADE,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    css_class VARCHAR(100),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

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

CREATE TABLE IF NOT EXISTS sys_operation_log_default PARTITION OF sys_operation_log DEFAULT;

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

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_created_at ON sys_operation_log (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_user_id ON sys_operation_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_module ON sys_operation_log (module);

INSERT INTO sys_dict_type (id, name, code, description, status)
SELECT gen_random_uuid(), '通用状态', 'common_status', '通用启用禁用状态字典', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'common_status');

INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, css_class, status)
SELECT gen_random_uuid(), 'common_status', '启用', '1', 1, 'success', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'common_status' AND value = '1'
);

INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, css_class, status)
SELECT gen_random_uuid(), 'common_status', '禁用', '0', 2, 'default', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'common_status' AND value = '0'
);
