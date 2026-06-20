--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-1-add-inventory-receipt-and-txn-tables-001
CREATE TABLE IF NOT EXISTS inventory_receipt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_no VARCHAR(50) NOT NULL UNIQUE,
    source_type VARCHAR(30) NOT NULL,
    source_order_id UUID,
    source_order_no VARCHAR(50),
    supplier_id UUID REFERENCES supplier(id),
    supplier_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);
