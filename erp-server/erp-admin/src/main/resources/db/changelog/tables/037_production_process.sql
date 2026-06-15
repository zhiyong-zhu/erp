--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-0-create-production-process-table-001
CREATE TABLE IF NOT EXISTS production_process (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    product_id UUID REFERENCES product(id),
    version VARCHAR(50) NOT NULL DEFAULT 'V1.0',
    status SMALLINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-1-create-production-process-product-index-001
CREATE INDEX IF NOT EXISTS idx_production_process_product ON production_process(product_id);

--changeset erp:v1-5-2-create-production-process-name-index-001
CREATE INDEX IF NOT EXISTS idx_production_process_name ON production_process(name);

--changeset erp:v1-5-3-create-production-process-status-index-001
CREATE INDEX IF NOT EXISTS idx_production_process_status ON production_process(status);
