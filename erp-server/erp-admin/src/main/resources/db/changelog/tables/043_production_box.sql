--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-22-create-production-box-table-001
CREATE TABLE IF NOT EXISTS production_box (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    box_code VARCHAR(100) NOT NULL UNIQUE,
    batch_id UUID NOT NULL REFERENCES production_batch(id),
    batch_no VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL REFERENCES product(id),
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    package_id UUID REFERENCES product_package(id),
    package_name VARCHAR(100),
    package_level INTEGER,
    quantity NUMERIC(14,4) NOT NULL,
    serial_nos TEXT,
    label_html TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PACKED',
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-23-create-production-box-batch-index-001
CREATE INDEX IF NOT EXISTS idx_production_box_batch ON production_box(batch_id);

--changeset erp:v1-5-24-create-production-box-product-index-001
CREATE INDEX IF NOT EXISTS idx_production_box_product ON production_box(product_id);

--changeset erp:v1-5-25-create-production-box-status-index-001
CREATE INDEX IF NOT EXISTS idx_production_box_status ON production_box(status);
