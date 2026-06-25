--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-6-2-add-inventory-check-workflow-columns-001
ALTER TABLE inventory_check
    ADD COLUMN IF NOT EXISTS reviewed_by UUID,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS review_remark VARCHAR(500),
    ADD COLUMN IF NOT EXISTS approved_by UUID,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approval_remark VARCHAR(500),
    ADD COLUMN IF NOT EXISTS rejected_by UUID,
    ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reject_remark VARCHAR(500);

--changeset erp:v1-6-2-add-inventory-check-item-position-columns-001
ALTER TABLE inventory_check_item
    ADD COLUMN IF NOT EXISTS warehouse_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS warehouse_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS location_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS location_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS batch_no VARCHAR(100);
