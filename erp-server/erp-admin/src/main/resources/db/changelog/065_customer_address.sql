--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-4-customer-address-001
CREATE TABLE IF NOT EXISTS customer_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer(id),
    recipient VARCHAR(100),
    phone VARCHAR(50),
    address VARCHAR(500) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-4-4-customer-address-002
CREATE INDEX IF NOT EXISTS idx_customer_address_customer ON customer_address (customer_id);

--changeset erp:v1-4-4-customer-address-003
-- 把客户已有的 address（非空）迁移为一条默认地址记录
INSERT INTO customer_address (customer_id, address, is_default)
SELECT id, address, TRUE FROM customer
WHERE address IS NOT NULL AND address <> ''
AND NOT EXISTS (
    SELECT 1 FROM customer_address WHERE customer_address.customer_id = customer.id
);
