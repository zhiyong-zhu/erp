--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-021
CREATE TABLE IF NOT EXISTS ecommerce_sku_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES ecommerce_shop(id),
    platform_sku_id VARCHAR(100) NOT NULL,
    platform_product_name VARCHAR(200),
    sku_id UUID,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-022
CREATE UNIQUE INDEX IF NOT EXISTS uk_ecommerce_sku_mapping ON ecommerce_sku_mapping (shop_id, platform_sku_id);
