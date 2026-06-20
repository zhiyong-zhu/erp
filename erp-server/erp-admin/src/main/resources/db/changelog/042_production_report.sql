--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-17-create-production-report-table-001
CREATE TABLE IF NOT EXISTS production_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_no VARCHAR(50) NOT NULL UNIQUE,
    batch_id UUID NOT NULL REFERENCES production_batch(id),
    batch_no VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL REFERENCES product(id),
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    report_quantity NUMERIC(14,4) NOT NULL,
    good_quantity NUMERIC(14,4) NOT NULL,
    defect_quantity NUMERIC(14,4) NOT NULL DEFAULT 0,
    report_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    operator_name VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-5-18-create-production-report-batch-index-001
CREATE INDEX IF NOT EXISTS idx_production_report_batch ON production_report(batch_id);

--changeset erp:v1-5-19-create-production-report-product-index-001
CREATE INDEX IF NOT EXISTS idx_production_report_product ON production_report(product_id);

--changeset erp:v1-5-20-create-production-report-status-index-001
CREATE INDEX IF NOT EXISTS idx_production_report_status ON production_report(status);

--changeset erp:v1-5-21-create-production-report-at-index-001
CREATE INDEX IF NOT EXISTS idx_production_report_at ON production_report(report_at);
