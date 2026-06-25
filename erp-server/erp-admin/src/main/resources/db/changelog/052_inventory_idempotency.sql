--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-8-1-add-inventory-idempotency-001
ALTER TABLE inventory_receipt
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(120);

--changeset erp:v1-8-1-add-inventory-idempotency-002
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_receipt_idempotency_key
    ON inventory_receipt(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

--changeset erp:v1-8-1-add-inventory-idempotency-003
ALTER TABLE inventory_issue
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(120);

--changeset erp:v1-8-1-add-inventory-idempotency-004
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_issue_idempotency_key
    ON inventory_issue(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
