CREATE TABLE IF NOT EXISTS production_product_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE REFERENCES product(id),
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    current_stock NUMERIC(14,4) NOT NULL DEFAULT 0,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);
