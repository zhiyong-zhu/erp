CREATE TABLE IF NOT EXISTS product_bom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    version VARCHAR(20) DEFAULT 'V1.0',
    status SMALLINT DEFAULT 1,
    effective_date DATE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS product_bom_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bom_id UUID NOT NULL REFERENCES product_bom(id) ON DELETE CASCADE,
    material_id UUID NOT NULL,
    material_type SMALLINT,
    quantity NUMERIC(12,4) NOT NULL,
    unit VARCHAR(20),
    loss_rate NUMERIC(5,2) DEFAULT 0,
    remark VARCHAR(200),
    sort_order INTEGER DEFAULT 0
);
