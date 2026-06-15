--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-3-add-product-bom-tables-001
CREATE TABLE IF NOT EXISTS product_bom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    version VARCHAR(20) DEFAULT 'V1.0',
    status SMALLINT DEFAULT 1,
    effective_date DATE,
    created_at TIMESTAMPTZ DEFAULT now()
);
