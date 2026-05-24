CREATE TABLE IF NOT EXISTS purchase_exception (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exception_no VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    purchase_order_item_id UUID REFERENCES purchase_order_item(id),
    supplier_id UUID REFERENCES supplier(id),
    supplier_name VARCHAR(200),
    material_id UUID REFERENCES material(id),
    material_code VARCHAR(50),
    material_name VARCHAR(200),
    exception_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description VARCHAR(500),
    resolution VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    handled_by UUID,
    handled_at TIMESTAMPTZ
);
