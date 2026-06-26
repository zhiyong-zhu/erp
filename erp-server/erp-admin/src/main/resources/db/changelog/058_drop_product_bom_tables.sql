--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

-- 产品 BOM 统一切换到生产 BOM（production_bom）模型，移除旧的 product_bom / product_bom_item 两张表。
-- 删除顺序：先子表 product_bom_item（其 bom_id 引用 product_bom），再父表 product_bom。

--changeset erp:v2-0-0-drop-product-bom-item-table-001
DROP TABLE IF EXISTS product_bom_item;

--changeset erp:v2-0-0-drop-product-bom-table-002
DROP TABLE IF EXISTS product_bom;
