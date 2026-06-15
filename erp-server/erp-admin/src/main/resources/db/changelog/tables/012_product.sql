--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-0-add-product-tables-002
CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES product_category(id),
    brand VARCHAR(100),
    unit VARCHAR(20) NOT NULL,
    description TEXT,
    images TEXT[],
    specifications JSONB,
    status SMALLINT DEFAULT 0,
    created_by UUID REFERENCES sys_user(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

--changeset erp:v1-1-0-add-product-tables-003
CREATE INDEX IF NOT EXISTS idx_product_code ON product(code);

--changeset erp:v1-1-0-add-product-tables-004
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);

--changeset erp:v1-1-0-add-product-tables-005
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);

--changeset erp:v1-1-0-add-product-tables-006
CREATE INDEX IF NOT EXISTS idx_product_name_gin ON product USING gin(to_tsvector('simple', name));
