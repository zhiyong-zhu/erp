--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-2-0-add-material-tables-002
CREATE TABLE IF NOT EXISTS supplier (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    contact_person VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    credit_rating SMALLINT,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-2-1-align-material-tables-with-base-entity-002
ALTER TABLE supplier
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID;
