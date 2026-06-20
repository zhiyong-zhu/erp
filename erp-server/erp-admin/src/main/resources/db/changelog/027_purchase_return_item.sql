--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-3-2-add-purchase-return-tables-002
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
