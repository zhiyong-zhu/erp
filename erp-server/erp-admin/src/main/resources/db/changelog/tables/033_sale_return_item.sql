--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-014
CREATE TABLE IF NOT EXISTS sale_return_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_return_id UUID NOT NULL REFERENCES sale_return(id) ON DELETE CASCADE,
    sale_order_item_id UUID,
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    quantity NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,4),
    return_amount NUMERIC(14,2),
    reason VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-015
CREATE INDEX IF NOT EXISTS idx_sale_return_item_return ON sale_return_item (sale_return_id);
