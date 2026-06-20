--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-6-1-add-inventory-transfer-table-001
CREATE TABLE IF NOT EXISTS inventory_transfer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_no VARCHAR(50) NOT NULL UNIQUE,
    from_location VARCHAR(100) NOT NULL,
    to_location VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-6-1-add-inventory-transfer-item-table-001
CREATE TABLE IF NOT EXISTS inventory_transfer_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id UUID NOT NULL REFERENCES inventory_transfer(id),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    remark VARCHAR(200)
);

--changeset erp:v1-6-1-add-inventory-transaction-transfer-id-001
ALTER TABLE inventory_transaction
    ADD COLUMN IF NOT EXISTS transfer_id UUID REFERENCES inventory_transfer(id);
