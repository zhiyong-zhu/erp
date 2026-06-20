--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-9-create-production-bom-item-table-001
CREATE TABLE IF NOT EXISTS production_bom_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bom_id UUID NOT NULL REFERENCES production_bom(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id),
    quantity NUMERIC(14,4) NOT NULL,
    unit VARCHAR(20),
    loss_rate NUMERIC(8,2) DEFAULT 0,
    process_step_no INTEGER,
    remark VARCHAR(500),
    CONSTRAINT uk_production_bom_material UNIQUE (bom_id, material_id)
);

--changeset erp:v1-5-10-create-production-bom-item-bom-index-001
CREATE INDEX IF NOT EXISTS idx_production_bom_item_bom ON production_bom_item(bom_id);

--changeset erp:v1-5-11-create-production-bom-item-material-index-001
CREATE INDEX IF NOT EXISTS idx_production_bom_item_material ON production_bom_item(material_id);
