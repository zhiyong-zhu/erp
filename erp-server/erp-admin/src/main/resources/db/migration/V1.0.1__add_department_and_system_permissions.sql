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

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sys_user_department'
    ) THEN
        ALTER TABLE sys_user
            ADD CONSTRAINT fk_sys_user_department
            FOREIGN KEY (department_id) REFERENCES sys_department(id)
            NOT VALID;
    END IF;
END $$;

INSERT INTO sys_department (id, parent_id, name, code, leader, sort_order, status)
SELECT gen_random_uuid(), NULL, '总部', 'HQ', '系统管理员', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE code = 'HQ');

INSERT INTO sys_department (id, parent_id, name, code, leader, sort_order, status)
SELECT gen_random_uuid(), hq.id, '系统管理部', 'SYS', '系统管理员', 1, 1
FROM sys_department hq
WHERE hq.code = 'HQ'
  AND NOT EXISTS (SELECT 1 FROM sys_department WHERE code = 'SYS');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '部门管理', 'system:department:list', 2, '/system/departments', 'apartment', 4, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:department:list');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '创建部门', 'system:department:create', 3, NULL, NULL, 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:department:create');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '更新部门', 'system:department:update', 3, NULL, NULL, 6, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:department:update');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '角色管理', 'system:role:list', 2, '/system/roles', 'team', 7, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:role:list');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '创建角色', 'system:role:create', 3, NULL, NULL, 8, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:role:create');

INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort_order, status)
SELECT gen_random_uuid(), NULL, '更新角色', 'system:role:update', 3, NULL, NULL, 9, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'system:role:update');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code IN (
    'system:department:list',
    'system:department:create',
    'system:department:update',
    'system:role:list',
    'system:role:create',
    'system:role:update'
)
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
