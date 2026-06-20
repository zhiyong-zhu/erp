--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-6-2-add-inventory-check-table-001
CREATE TABLE IF NOT EXISTS inventory_check (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    check_no VARCHAR(50) NOT NULL UNIQUE,
    check_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_difference NUMERIC(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-6-2-add-inventory-check-item-table-001
CREATE TABLE IF NOT EXISTS inventory_check_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    check_id UUID NOT NULL REFERENCES inventory_check(id),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    system_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    actual_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    difference_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(200)
);

--changeset erp:v1-6-2-add-inventory-transaction-check-id-001
ALTER TABLE inventory_transaction
    ADD COLUMN IF NOT EXISTS check_id UUID REFERENCES inventory_check(id);
