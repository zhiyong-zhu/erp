--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-26-create-production-product-stock-table-001
CREATE TABLE IF NOT EXISTS production_product_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES product(id),
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    current_stock NUMERIC(14,4) NOT NULL DEFAULT 0,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-27-create-production-product-stock-name-index-001
CREATE INDEX IF NOT EXISTS idx_production_product_stock_name ON production_product_stock(product_name);
