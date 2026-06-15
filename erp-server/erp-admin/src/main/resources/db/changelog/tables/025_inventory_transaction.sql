--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-1-add-inventory-receipt-and-txn-tables-002
CREATE TABLE IF NOT EXISTS inventory_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    source_type VARCHAR(30),
    source_order_id UUID,
    source_order_no VARCHAR(50),
    source_item_id UUID,
    receipt_id UUID REFERENCES inventory_receipt(id),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);
