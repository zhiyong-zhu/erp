--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-1-add-department-and-system-permissions-001
CREATE TABLE IF NOT EXISTS sys_department (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES sys_department(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    leader VARCHAR(50),
    phone VARCHAR(20),
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-0-1-add-department-and-system-permissions-003
INSERT INTO sys_department (id, parent_id, name, code, leader, sort_order, status)
SELECT gen_random_uuid(), NULL, '总部', 'HQ', '系统管理员', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE code = 'HQ');

--changeset erp:v1-0-1-add-department-and-system-permissions-004
INSERT INTO sys_department (id, parent_id, name, code, leader, sort_order, status)
SELECT gen_random_uuid(), hq.id, '系统管理部', 'SYS', '系统管理员', 1, 1
FROM sys_department hq
WHERE hq.code = 'HQ'
  AND NOT EXISTS (SELECT 1 FROM sys_department WHERE code = 'SYS');
