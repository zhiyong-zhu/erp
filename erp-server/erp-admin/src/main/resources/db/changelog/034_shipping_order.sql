--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-016
CREATE TABLE IF NOT EXISTS shipping_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_order_id UUID NOT NULL REFERENCES sale_order(id),
    carrier_code VARCHAR(50),
    carrier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipped_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-017
CREATE INDEX IF NOT EXISTS idx_shipping_order_sale ON shipping_order (sale_order_id);
