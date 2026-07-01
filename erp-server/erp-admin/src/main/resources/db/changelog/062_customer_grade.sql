--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-customer-grade-001
ALTER TABLE customer ADD COLUMN grade VARCHAR(1) DEFAULT 'C';

--changeset erp:v1-4-0-customer-grade-002
ALTER TABLE customer DROP COLUMN IF EXISTS credit_limit;
