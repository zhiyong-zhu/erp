--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-0-add-product-tables-007
CREATE TABLE IF NOT EXISTS product_sku (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    attributes JSONB NOT NULL,
    barcode VARCHAR(100),
    price NUMERIC(12,2),
    cost_price NUMERIC(12,2),
    weight NUMERIC(10,3),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-1-0-add-product-tables-008
CREATE INDEX IF NOT EXISTS idx_sku_product ON product_sku(product_id);

--changeset erp:v1-1-0-add-product-tables-009
CREATE INDEX IF NOT EXISTS idx_sku_barcode ON product_sku(barcode);
