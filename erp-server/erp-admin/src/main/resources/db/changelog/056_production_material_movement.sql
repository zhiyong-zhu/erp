--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v0-9-1-create-production-material-movement-table-001
CREATE TABLE IF NOT EXISTS production_material_movement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movement_no VARCHAR(50) NOT NULL UNIQUE,
    movement_type VARCHAR(30) NOT NULL,
    batch_id UUID NOT NULL REFERENCES production_batch(id),
    batch_no VARCHAR(50) NOT NULL,
    inventory_document_id UUID,
    inventory_document_no VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_quantity NUMERIC(14,4) NOT NULL DEFAULT 0,
    warehouse_code VARCHAR(50),
    warehouse_name VARCHAR(100),
    location_code VARCHAR(50),
    location_name VARCHAR(100),
    batch_no_inventory VARCHAR(100),
    idempotency_key VARCHAR(100),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v0-9-1-create-production-material-movement-idempotency-index-001
CREATE UNIQUE INDEX IF NOT EXISTS uk_production_material_movement_idempotency
    ON production_material_movement(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

--changeset erp:v0-9-1-create-production-material-movement-batch-index-001
CREATE INDEX IF NOT EXISTS idx_production_material_movement_batch
    ON production_material_movement(batch_id);

--changeset erp:v0-9-1-create-production-material-movement-item-table-001
CREATE TABLE IF NOT EXISTS production_material_movement_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movement_id UUID NOT NULL REFERENCES production_material_movement(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50),
    material_name VARCHAR(200),
    quantity NUMERIC(14,4) NOT NULL,
    warehouse_code VARCHAR(50),
    warehouse_name VARCHAR(100),
    location_code VARCHAR(50),
    location_name VARCHAR(100),
    batch_no VARCHAR(100),
    remark VARCHAR(500)
);

--changeset erp:v0-9-1-create-production-material-movement-item-index-001
CREATE INDEX IF NOT EXISTS idx_production_material_movement_item_movement
    ON production_material_movement_item(movement_id);

--changeset erp:v0-9-1-create-production-material-movement-material-index-001
CREATE INDEX IF NOT EXISTS idx_production_material_movement_item_material
    ON production_material_movement_item(material_id);
