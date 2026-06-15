--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-28-create-serial-number-table-001
CREATE TABLE IF NOT EXISTS serial_number (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serial_no VARCHAR(100) NOT NULL UNIQUE,
    batch_id UUID REFERENCES production_batch(id),
    product_id UUID NOT NULL REFERENCES product(id),
    status VARCHAR(30) NOT NULL DEFAULT 'GENERATED',
    produced_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-29-create-serial-number-batch-index-001
CREATE INDEX IF NOT EXISTS idx_serial_number_batch ON serial_number(batch_id);

--changeset erp:v1-5-30-create-serial-number-product-index-001
CREATE INDEX IF NOT EXISTS idx_serial_number_product ON serial_number(product_id);

--changeset erp:v1-5-31-create-serial-number-status-index-001
CREATE INDEX IF NOT EXISTS idx_serial_number_status ON serial_number(status);
