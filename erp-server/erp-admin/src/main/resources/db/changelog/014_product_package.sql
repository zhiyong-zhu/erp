--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-1-add-product-package-table-001
CREATE TABLE IF NOT EXISTS product_package (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    level SMALLINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    weight NUMERIC(10,3),
    dimensions JSONB,
    barcode VARCHAR(100),
    label_template_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(product_id, level)
);
