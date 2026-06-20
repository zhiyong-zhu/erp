--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-2-0-add-material-tables-001
CREATE TABLE IF NOT EXISTS material_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES material_category(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-2-1-align-material-tables-with-base-entity-001
ALTER TABLE material_category
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID;
