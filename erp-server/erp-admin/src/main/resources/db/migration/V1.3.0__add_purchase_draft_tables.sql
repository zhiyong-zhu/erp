CREATE TABLE IF NOT EXISTS purchase_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id UUID NOT NULL REFERENCES supplier(id),
    supplier_name VARCHAR(200) NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(14,2) DEFAULT 0,
    source_type VARCHAR(30),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS purchase_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    unit VARCHAR(20),
    quantity NUMERIC(12,2) NOT NULL,
    quote_price NUMERIC(12,2),
    estimated_amount NUMERIC(14,2),
    lead_time_days INTEGER,
    source_type VARCHAR(30),
    source_ref_id UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS received_at TIMESTAMPTZ;

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS received_quantity NUMERIC(12,2) DEFAULT 0;

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS accepted_quantity NUMERIC(12,2) DEFAULT 0;

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS rejected_quantity NUMERIC(12,2) DEFAULT 0;

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS inspection_result VARCHAR(20);

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS exception_reason VARCHAR(500);
