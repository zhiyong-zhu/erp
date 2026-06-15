--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-4-create-production-process-step-table-001
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

--changeset erp:v1-5-5-create-production-process-step-process-index-001
CREATE INDEX IF NOT EXISTS idx_production_process_step_process ON production_process_step(process_id);
