--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-12-create-production-batch-table-001
CREATE TABLE IF NOT EXISTS production_batch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_no VARCHAR(50) NOT NULL UNIQUE,
    product_id UUID NOT NULL REFERENCES product(id),
    planned_quantity NUMERIC(14,4) NOT NULL,
    completed_quantity NUMERIC(14,4) DEFAULT 0,
    unit VARCHAR(20),
    process_id UUID REFERENCES production_process(id),
    bom_id UUID REFERENCES production_bom(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    planned_start_date DATE,
    planned_end_date DATE,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-13-create-production-batch-product-index-001
CREATE INDEX IF NOT EXISTS idx_production_batch_product ON production_batch(product_id);

--changeset erp:v1-5-14-create-production-batch-status-index-001
CREATE INDEX IF NOT EXISTS idx_production_batch_status ON production_batch(status);

--changeset erp:v1-5-15-create-production-batch-process-index-001
CREATE INDEX IF NOT EXISTS idx_production_batch_process ON production_batch(process_id);

--changeset erp:v1-5-16-create-production-batch-bom-index-001
CREATE INDEX IF NOT EXISTS idx_production_batch_bom ON production_batch(bom_id);
