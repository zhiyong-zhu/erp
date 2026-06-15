--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-0-add-sales-tables-004
CREATE TABLE IF NOT EXISTS sale_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customer(id),
    customer_name VARCHAR(200) NOT NULL,
    order_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    platform_order_no VARCHAR(100),
    platform_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CONFIRM',
    total_amount NUMERIC(14,2) DEFAULT 0,
    discount_amount NUMERIC(14,2) DEFAULT 0,
    freight_amount NUMERIC(14,2) DEFAULT 0,
    payable_amount NUMERIC(14,2) DEFAULT 0,
    paid_amount NUMERIC(14,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    shipping_address JSONB,
    remark VARCHAR(500),
    ordered_at TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-0-add-sales-tables-005
CREATE INDEX IF NOT EXISTS idx_sale_order_customer ON sale_order (customer_id);

--changeset erp:v1-4-0-add-sales-tables-006
CREATE INDEX IF NOT EXISTS idx_sale_order_status ON sale_order (status);

--changeset erp:v1-4-0-add-sales-tables-007
CREATE INDEX IF NOT EXISTS idx_sale_order_source ON sale_order (order_source);

--changeset erp:v1-4-0-add-sales-tables-008
CREATE INDEX IF NOT EXISTS idx_sale_order_platform ON sale_order (platform_order_no) WHERE platform_order_no IS NOT NULL;
