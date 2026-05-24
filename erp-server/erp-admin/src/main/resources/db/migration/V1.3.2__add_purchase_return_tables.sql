CREATE TABLE IF NOT EXISTS purchase_return (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_no VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id),
    purchase_order_no VARCHAR(50) NOT NULL,
    supplier_id UUID REFERENCES supplier(id),
    supplier_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_amount NUMERIC(14,2) DEFAULT 0,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS purchase_return_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_return_id UUID NOT NULL REFERENCES purchase_return(id),
    purchase_order_item_id UUID NOT NULL REFERENCES purchase_order_item(id),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    unit VARCHAR(20),
    return_quantity NUMERIC(12,2) NOT NULL,
    quote_price NUMERIC(12,2),
    return_amount NUMERIC(14,2),
    reason VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE purchase_order_item
    ADD COLUMN IF NOT EXISTS returned_quantity NUMERIC(12,2) DEFAULT 0;
