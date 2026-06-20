--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-1-0-add-product-tables-001
CREATE TABLE IF NOT EXISTS product_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES product_category(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-1-0-add-product-tables-010
INSERT INTO product_category (id, parent_id, name, code, sort_order, status)
SELECT gen_random_uuid(), NULL, '默认分类', 'DEFAULT', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM product_category WHERE code = 'DEFAULT');
