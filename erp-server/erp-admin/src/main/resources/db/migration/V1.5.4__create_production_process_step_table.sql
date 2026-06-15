CREATE TABLE IF NOT EXISTS production_process_step (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id UUID NOT NULL REFERENCES production_process(id) ON DELETE CASCADE,
    step_no INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    workstation VARCHAR(100),
    standard_minutes NUMERIC(10,2),
    quality_requirement VARCHAR(500),
    remark VARCHAR(500),
    CONSTRAINT uk_production_process_step_no UNIQUE (process_id, step_no)
);
