--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-1-add-sale-exception-table-001
CREATE TABLE IF NOT EXISTS sale_exception (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exception_no VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id UUID REFERENCES sale_order(id),
    sale_order_item_id UUID REFERENCES sale_order_item(id),
    sale_return_id UUID REFERENCES sale_return(id),
    sale_return_item_id UUID REFERENCES sale_return_item(id),
    customer_id UUID REFERENCES customer(id),
    customer_name VARCHAR(200),
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    exception_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description VARCHAR(500),
    resolution VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    handled_by UUID,
    handled_at TIMESTAMPTZ
);

--changeset erp:v1-4-1-add-sale-exception-table-002
CREATE INDEX IF NOT EXISTS idx_sale_exception_order ON sale_exception (sale_order_id);

--changeset erp:v1-4-1-add-sale-exception-table-003
CREATE INDEX IF NOT EXISTS idx_sale_exception_return ON sale_exception (sale_return_id);

--changeset erp:v1-4-1-add-sale-exception-table-004
CREATE INDEX IF NOT EXISTS idx_sale_exception_status ON sale_exception (status);
