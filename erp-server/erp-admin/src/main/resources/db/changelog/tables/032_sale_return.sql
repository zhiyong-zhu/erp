--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-011
CREATE TABLE IF NOT EXISTS sale_return (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_no VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id UUID NOT NULL REFERENCES sale_order(id),
    sale_order_no VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW',
    total_amount NUMERIC(14,2) DEFAULT 0,
    reason VARCHAR(500),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-012
CREATE INDEX IF NOT EXISTS idx_sale_return_order ON sale_return (sale_order_id);

--changeset erp:v1-4-0-add-sales-tables-013
CREATE INDEX IF NOT EXISTS idx_sale_return_status ON sale_return (status);
