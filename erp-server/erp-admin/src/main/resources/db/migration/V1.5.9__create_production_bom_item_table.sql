CREATE TABLE IF NOT EXISTS production_bom_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bom_id UUID NOT NULL REFERENCES production_bom(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id),
    quantity NUMERIC(14,4) NOT NULL,
    unit VARCHAR(20),
    loss_rate NUMERIC(8,2) DEFAULT 0,
    process_step_no INTEGER,
    remark VARCHAR(500),
    CONSTRAINT uk_production_bom_material UNIQUE (bom_id, material_id)
);
