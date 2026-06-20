--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-0-init-auth-and-user-tables-002
CREATE TABLE IF NOT EXISTS sys_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500),
    department_id UUID,
    status SMALLINT DEFAULT 1,
    last_login_at TIMESTAMPTZ,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

--changeset erp:v1-0-0-init-auth-and-user-tables-008
INSERT INTO sys_user (id, username, password, real_name, status, deleted)
SELECT gen_random_uuid(),
       'admin',
       '$2b$12$KrIaf4sXVQDm2kJ48P18kuJoC0SFrrc2fHza4kEx/TaPsduzGYGx6',
       '系统管理员',
       1,
       FALSE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');
