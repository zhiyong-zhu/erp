--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-2-sys-param-001
CREATE TABLE IF NOT EXISTS sys_param (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(500) NOT NULL,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description VARCHAR(500),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-2-sys-param-002
COMMENT ON COLUMN sys_param.value_type IS '值类型：BOOL / STRING / INT，便于前端按类型渲染';

--changeset erp:v1-4-2-sys-param-003
CREATE INDEX IF NOT EXISTS idx_sys_param_code ON sys_param (code);

--changeset erp:v1-4-2-sys-param-004
INSERT INTO sys_param (code, name, value, value_type, description) VALUES
    ('sale_order.confirm_reserve_stock', '订单确认校验并锁定库存', 'true', 'BOOL',
     '开启后，确认销售订单时会校验并预占库存；关闭后确认订单不校验、不锁定库存，直接进入待发货')
ON CONFLICT (code) DO NOTHING;
