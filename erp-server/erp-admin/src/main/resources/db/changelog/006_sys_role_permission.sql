--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-0-init-auth-and-user-tables-006
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id UUID NOT NULL REFERENCES sys_role(id),
    permission_id UUID NOT NULL REFERENCES sys_permission(id),
    PRIMARY KEY (role_id, permission_id)
);
