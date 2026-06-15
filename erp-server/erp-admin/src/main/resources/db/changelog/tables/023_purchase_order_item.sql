--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-0-add-purchase-draft-tables-002
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    unit VARCHAR(20),
    quantity NUMERIC(12,2) NOT NULL,
    quote_price NUMERIC(12,2),
    estimated_amount NUMERIC(14,2),
    lead_time_days INTEGER,
    source_type VARCHAR(30),
    source_ref_id UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-3-0-add-purchase-draft-tables-004
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS received_quantity NUMERIC(12,2) DEFAULT 0;

--changeset erp:v1-3-0-add-purchase-draft-tables-005
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS accepted_quantity NUMERIC(12,2) DEFAULT 0;

--changeset erp:v1-3-0-add-purchase-draft-tables-006
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS rejected_quantity NUMERIC(12,2) DEFAULT 0;

--changeset erp:v1-3-0-add-purchase-draft-tables-007
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS inspection_result VARCHAR(20);

--changeset erp:v1-3-0-add-purchase-draft-tables-008
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS exception_reason VARCHAR(500);

--changeset erp:v1-3-2-add-purchase-return-tables-003
ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS returned_quantity NUMERIC(12,2) DEFAULT 0;
