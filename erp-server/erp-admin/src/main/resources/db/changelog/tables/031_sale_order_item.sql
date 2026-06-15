--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-009
CREATE TABLE IF NOT EXISTS sale_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_order_id UUID NOT NULL REFERENCES sale_order(id) ON DELETE CASCADE,
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    unit VARCHAR(20),
    quantity NUMERIC(12,2) NOT NULL,
    shipped_quantity NUMERIC(12,2) DEFAULT 0,
    unit_price NUMERIC(12,4),
    amount NUMERIC(14,2),
    remark VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-010
CREATE INDEX IF NOT EXISTS idx_sale_item_order ON sale_order_item (sale_order_id);
