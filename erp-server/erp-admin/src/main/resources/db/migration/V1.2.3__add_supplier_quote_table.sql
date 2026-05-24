CREATE TABLE IF NOT EXISTS supplier_quote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID NOT NULL REFERENCES supplier(id),
    material_id UUID NOT NULL REFERENCES material(id),
    quote_price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    min_order_quantity NUMERIC(12,2),
    lead_time_days INTEGER,
    remark VARCHAR(500),
    effective_date DATE,
    expiry_date DATE,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_supplier_quote_supplier_material_effective
    ON supplier_quote (supplier_id, material_id, effective_date);
