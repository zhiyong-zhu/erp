--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-018
CREATE TABLE IF NOT EXISTS ecommerce_shop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    platform VARCHAR(20) NOT NULL,
    shop_name VARCHAR(200) NOT NULL,
    shop_id_on_platform VARCHAR(100) NOT NULL,
    access_token VARCHAR(500),
    refresh_token VARCHAR(500),
    token_expires_at TIMESTAMPTZ,
    sync_config JSONB,
    status SMALLINT DEFAULT 1,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-019
CREATE INDEX IF NOT EXISTS idx_ecommerce_shop_platform ON ecommerce_shop (platform);

--changeset erp:v1-4-0-add-sales-tables-020
CREATE UNIQUE INDEX IF NOT EXISTS uk_ecommerce_shop_platform_id ON ecommerce_shop (platform, shop_id_on_platform);
