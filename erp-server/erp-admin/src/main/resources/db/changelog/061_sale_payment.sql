--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

-- 销售收款流水表，支持多次部分收款和审计追溯。
-- sale_order 表已有 paid_amount/payment_status/paid_at 字段（030_sale_order.sql），无需再加。

--changeset erp:v2-0-2-create-sale-payment-table-001
CREATE TABLE IF NOT EXISTS sale_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id UUID NOT NULL REFERENCES sale_order(id),
    sale_order_no VARCHAR(50) NOT NULL,
    customer_id UUID,
    customer_name VARCHAR(200),
    received_amount NUMERIC(14,2) NOT NULL,
    payment_method VARCHAR(30),
    payment_time TIMESTAMPTZ DEFAULT now(),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v2-0-2-create-sale-payment-index-001
CREATE INDEX IF NOT EXISTS idx_sale_payment_order ON sale_payment(sale_order_id);
