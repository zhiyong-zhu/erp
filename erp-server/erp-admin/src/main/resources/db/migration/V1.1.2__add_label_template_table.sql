CREATE TABLE IF NOT EXISTS label_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    width_mm NUMERIC(6,1) NOT NULL,
    height_mm NUMERIC(6,1) NOT NULL,
    template_config JSONB NOT NULL,
    preview_image VARCHAR(500),
    status SMALLINT DEFAULT 1,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
