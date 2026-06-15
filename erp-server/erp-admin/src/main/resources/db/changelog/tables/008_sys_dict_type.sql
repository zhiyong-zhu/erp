--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-3-add-dict-and-operation-log-001
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-0-3-add-dict-and-operation-log-009
INSERT INTO sys_dict_type (id, name, code, description, status)
SELECT gen_random_uuid(), '通用状态', 'common_status', '通用启用禁用状态字典', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'common_status');
