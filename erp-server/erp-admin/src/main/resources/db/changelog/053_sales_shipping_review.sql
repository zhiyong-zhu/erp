--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v0-8-0-add-product-reserved-stock-001
ALTER TABLE production_product_stock
    ADD COLUMN IF NOT EXISTS reserved_stock NUMERIC(14,4) NOT NULL DEFAULT 0;

--changeset erp:v0-8-0-create-shipping-order-item-table-001
CREATE TABLE IF NOT EXISTS shipping_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipping_order_id UUID NOT NULL REFERENCES shipping_order(id) ON DELETE CASCADE,
    sale_order_item_id UUID NOT NULL REFERENCES sale_order_item(id),
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    quantity NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v0-8-0-create-shipping-order-item-index-001
CREATE INDEX IF NOT EXISTS idx_shipping_item_shipping ON shipping_order_item(shipping_order_id);

--changeset erp:v0-8-0-create-shipping-order-sale-item-index-001
CREATE INDEX IF NOT EXISTS idx_shipping_item_sale_item ON shipping_order_item(sale_order_item_id);
