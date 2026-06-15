--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-2-0-add-material-tables-003
CREATE TABLE IF NOT EXISTS material (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES material_category(id),
    unit VARCHAR(20) NOT NULL,
    specifications VARCHAR(500),
    default_supplier_id UUID REFERENCES supplier(id),
    safety_stock NUMERIC(12,2) DEFAULT 0,
    lead_time_days INTEGER,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-2-1-align-material-tables-with-base-entity-003
ALTER TABLE material
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID;

--changeset erp:v1-2-2-enhance-material-stock-and-alerts-001
ALTER TABLE material
    ADD COLUMN IF NOT EXISTS current_stock NUMERIC(12,2) DEFAULT 0;
