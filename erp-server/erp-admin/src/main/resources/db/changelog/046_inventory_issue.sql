--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-6-0-add-inventory-issue-table-001
CREATE TABLE IF NOT EXISTS inventory_issue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_no VARCHAR(50) NOT NULL UNIQUE,
    issue_type VARCHAR(30) NOT NULL,
    source_order_id UUID,
    source_order_no VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    total_quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-6-0-add-inventory-transaction-issue-id-001
ALTER TABLE inventory_transaction
    ADD COLUMN IF NOT EXISTS issue_id UUID REFERENCES inventory_issue(id);
