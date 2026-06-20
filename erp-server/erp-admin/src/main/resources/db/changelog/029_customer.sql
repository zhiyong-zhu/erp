--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-001
CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    customer_type SMALLINT DEFAULT 1,
    contact_person VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(200),
    address VARCHAR(500),
    credit_limit NUMERIC(14,2) DEFAULT 0,
    payment_terms SMALLINT DEFAULT 1,
    sales_rep_id UUID,
    tax_number VARCHAR(50),
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-002
CREATE INDEX IF NOT EXISTS idx_customer_name ON customer (name);

--changeset erp:v1-4-0-add-sales-tables-003
CREATE INDEX IF NOT EXISTS idx_customer_status ON customer (status);
