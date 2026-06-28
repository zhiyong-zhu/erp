--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

-- 采购付款流水表，支持多次部分付款和审计追溯。

--changeset erp:v2-0-1-create-purchase-payment-table-001
CREATE TABLE IF NOT EXISTS purchase_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    purchase_order_no VARCHAR(50) NOT NULL,
    supplier_id UUID,
    supplier_name VARCHAR(200),
    paid_amount NUMERIC(14,2) NOT NULL,
    payment_method VARCHAR(30),
    payment_time TIMESTAMPTZ DEFAULT now(),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v2-0-1-create-purchase-payment-index-001
CREATE INDEX IF NOT EXISTS idx_purchase_payment_order ON purchase_payment(purchase_order_id);

--changeset erp:v2-0-1-add-purchase-order-payment-fields-001
ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(14,2) DEFAULT 0;
ALTER TABLE purchase_order ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'UNPAID';
