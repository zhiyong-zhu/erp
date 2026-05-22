CREATE EXTENSION IF NOT EXISTS pgcrypto;

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

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id UUID NOT NULL REFERENCES sys_user(id),
    role_id UUID NOT NULL REFERENCES sys_role(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES sys_permission(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    type SMALLINT,
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id UUID NOT NULL REFERENCES sys_role(id),
    permission_id UUID NOT NULL REFERENCES sys_permission(id),
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO sys_role (id, name, code, description, data_scope, status)
SELECT gen_random_uuid(), '系统管理员', 'ADMIN', '默认系统管理员角色', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'ADMIN');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '用户管理', 'system:user:list', 2, '/system/users', 'user', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:user:list');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '创建用户', 'system:user:create', 3, NULL, NULL, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:user:create');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '更新用户', 'system:user:update', 3, NULL, NULL, 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:user:update');

INSERT INTO sys_user (id, username, password, real_name, status, deleted)
SELECT gen_random_uuid(),
       'admin',
       '$2b$12$KrIaf4sXVQDm2kJ48P18kuJoC0SFrrc2fHza4kEx/TaPsduzGYGx6',
       '系统管理员',
       1,
       FALSE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.code = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code IN ('system:user:list', 'system:user:create', 'system:user:update')
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
