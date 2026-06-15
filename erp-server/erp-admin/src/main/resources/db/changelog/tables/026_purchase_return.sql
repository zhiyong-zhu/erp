--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-2-add-purchase-return-tables-001
CREATE TABLE IF NOT EXISTS purchase_return (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_no VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    purchase_order_no VARCHAR(50) NOT NULL,
    supplier_id UUID REFERENCES supplier(id),
    supplier_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_amount NUMERIC(14,2) DEFAULT 0,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);
