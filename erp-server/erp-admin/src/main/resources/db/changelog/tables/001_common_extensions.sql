--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-0-0-init-auth-and-user-tables-001
CREATE EXTENSION IF NOT EXISTS pgcrypto;
