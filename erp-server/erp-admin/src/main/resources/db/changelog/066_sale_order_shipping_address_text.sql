--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-4-shipping-address-text-001
-- 收货地址是普通字符串（非 JSON），把列类型从 JSONB 改为 TEXT，避免写入裸字符串时报 JSON 解析错误
ALTER TABLE sale_order ALTER COLUMN shipping_address TYPE TEXT USING shipping_address::text;
