CREATE TABLE IF NOT EXISTS production_bom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    product_id UUID NOT NULL REFERENCES product(id),
    version VARCHAR(50) NOT NULL DEFAULT 'V1.0',
    status SMALLINT NOT NULL DEFAULT 1,
    effective_date DATE,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);
