--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-3-add-dict-and-operation-log-002
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

--changeset erp:v1-0-3-add-dict-and-operation-log-010
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, css_class, status)
SELECT gen_random_uuid(), 'common_status', '启用', '1', 1, 'success', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'common_status' AND value = '1'
);

--changeset erp:v1-0-3-add-dict-and-operation-log-011
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, css_class, status)
SELECT gen_random_uuid(), 'common_status', '禁用', '0', 2, 'default', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'common_status' AND value = '0'
);
