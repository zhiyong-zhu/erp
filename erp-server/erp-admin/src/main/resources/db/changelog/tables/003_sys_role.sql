--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-0-init-auth-and-user-tables-003
CREATE TABLE IF NOT EXISTS sys_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    data_scope SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-0-0-init-auth-and-user-tables-007
INSERT INTO sys_role (id, name, code, description, data_scope, status)
SELECT gen_random_uuid(), '系统管理员', 'ADMIN', '默认系统管理员角色', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'ADMIN');
