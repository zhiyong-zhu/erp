--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-3-add-product-bom-tables-002
CREATE TABLE IF NOT EXISTS product_bom_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bom_id UUID NOT NULL REFERENCES product_bom(id) ON DELETE CASCADE,
    material_id UUID NOT NULL,
    material_type SMALLINT,
    quantity NUMERIC(12,4) NOT NULL,
    unit VARCHAR(20),
    loss_rate NUMERIC(5,2) DEFAULT 0,
    remark VARCHAR(200),
    sort_order INTEGER DEFAULT 0
);
