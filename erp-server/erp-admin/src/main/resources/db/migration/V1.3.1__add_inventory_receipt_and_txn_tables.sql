CREATE TABLE IF NOT EXISTS inventory_receipt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_no VARCHAR(50) NOT NULL UNIQUE,
    source_type VARCHAR(30) NOT NULL,
    source_order_id UUID,
    source_order_no VARCHAR(50),
    supplier_id UUID REFERENCES supplier(id),
    supplier_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS inventory_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES material(id),
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    source_type VARCHAR(30),
    source_order_id UUID,
    source_order_no VARCHAR(50),
    source_item_id UUID,
    receipt_id UUID REFERENCES inventory_receipt(id),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);
