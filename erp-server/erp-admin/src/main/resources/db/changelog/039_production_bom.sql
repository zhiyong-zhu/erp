--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-6-create-production-bom-table-001
CREATE TABLE IF NOT EXISTS production_bom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    product_id UUID NOT NULL REFERENCES product(id),
    version VARCHAR(50) NOT NULL DEFAULT 'V1.0',
    status SMALLINT NOT NULL DEFAULT 1,
    effective_date DATE,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-7-create-production-bom-product-index-001
CREATE INDEX IF NOT EXISTS idx_production_bom_product ON production_bom(product_id);

--changeset erp:v1-5-8-create-production-bom-status-index-001
CREATE INDEX IF NOT EXISTS idx_production_bom_status ON production_bom(status);
