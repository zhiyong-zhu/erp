--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-5-32-allow-product-inventory-transaction-001
ALTER TABLE inventory_transaction
    ALTER COLUMN material_id DROP NOT NULL,
    ALTER COLUMN material_code DROP NOT NULL,
    ALTER COLUMN material_name DROP NOT NULL;

--changeset erp:v1-5-32-allow-product-inventory-transaction-002
ALTER TABLE inventory_transaction
    DROP CONSTRAINT IF EXISTS inventory_transaction_material_id_fkey;
