--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v0-9-0-add-shipping-order-item-serial-nos-001
ALTER TABLE shipping_order_item
    ADD COLUMN IF NOT EXISTS serial_nos TEXT;
