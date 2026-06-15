CREATE TABLE IF NOT EXISTS serial_number (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serial_no VARCHAR(100) NOT NULL UNIQUE,
    batch_id UUID REFERENCES production_batch(id),
    product_id UUID NOT NULL REFERENCES product(id),
    status VARCHAR(30) NOT NULL DEFAULT 'GENERATED',
    produced_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);
