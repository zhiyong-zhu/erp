--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-0-add-purchase-draft-tables-001
CREATE TABLE IF NOT EXISTS purchase_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id UUID NOT NULL REFERENCES supplier(id),
    supplier_name VARCHAR(200) NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(14,2) DEFAULT 0,
    source_type VARCHAR(30),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-3-0-add-purchase-draft-tables-003
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS received_at TIMESTAMPTZ;
