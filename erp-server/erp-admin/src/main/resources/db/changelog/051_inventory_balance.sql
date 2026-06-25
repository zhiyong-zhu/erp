--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-8-0-add-inventory-balance-table-001
CREATE TABLE IF NOT EXISTS inventory_balance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    warehouse_code VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    warehouse_name VARCHAR(100) NOT NULL DEFAULT '主仓',
    location_code VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    location_name VARCHAR(100) NOT NULL DEFAULT '默认库位',
    batch_no VARCHAR(100) NOT NULL DEFAULT 'DEFAULT',
    available_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    frozen_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uk_inventory_balance_material_location_batch UNIQUE (material_id, warehouse_code, location_code, batch_no)
);

--changeset erp:v1-8-0-add-inventory-balance-table-002
CREATE INDEX IF NOT EXISTS idx_inventory_balance_material_id ON inventory_balance(material_id);

--changeset erp:v1-8-0-add-inventory-balance-table-003
ALTER TABLE inventory_transaction
    ADD COLUMN IF NOT EXISTS warehouse_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS warehouse_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS location_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS location_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS batch_no VARCHAR(100),
    ADD COLUMN IF NOT EXISTS balance_before NUMERIC(12,2);
