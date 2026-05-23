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

CREATE INDEX IF NOT EXISTS idx_product_code ON product(code);
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_product_name_gin ON product USING gin(to_tsvector('simple', name));

CREATE TABLE IF NOT EXISTS product_sku (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    attributes JSONB NOT NULL,
    barcode VARCHAR(100),
    price NUMERIC(12,2),
    cost_price NUMERIC(12,2),
    weight NUMERIC(10,3),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sku_product ON product_sku(product_id);
CREATE INDEX IF NOT EXISTS idx_sku_barcode ON product_sku(barcode);

INSERT INTO product_category (id, parent_id, name, code, sort_order, status)
SELECT gen_random_uuid(), NULL, '默认分类', 'DEFAULT', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM product_category WHERE code = 'DEFAULT');
