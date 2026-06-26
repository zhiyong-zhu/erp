--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

-- 生产 BOM 物料项支持「半成品」(product 表) 作为用料。
-- 1. product 表加 is_semifinished 标记，用于标识可作为 BOM 用料的半成品产品
-- 2. production_bom_item 去掉对 material 表的硬外键（半成品 id 在 product 表，外键无法同时指向两张表）
-- 3. production_bom_item 加 item_type: 1=原料(material 表), 2=半成品(product 表)；历史数据默认 1
-- 4. 重建唯一约束，把 item_type 纳入，保证「同类型同物料」在一个 BOM 内唯一

--changeset erp:v2-0-0-add-product-semifinished-flag-001
ALTER TABLE product ADD COLUMN IF NOT EXISTS is_semifinished BOOLEAN NOT NULL DEFAULT FALSE;

--changeset erp:v2-0-0-add-product-semifinished-index-001
CREATE INDEX IF NOT EXISTS idx_product_semifinished ON product(is_semifinished) WHERE is_semifinished = TRUE;

--changeset erp:v2-0-0-drop-production-bom-item-material-fk-001
ALTER TABLE production_bom_item DROP CONSTRAINT IF EXISTS production_bom_item_material_id_fkey;

--changeset erp:v2-0-0-add-production-bom-item-type-001
ALTER TABLE production_bom_item ADD COLUMN IF NOT EXISTS item_type SMALLINT NOT NULL DEFAULT 1;

--changeset erp:v2-0-0-rebuild-production-bom-item-unique-001
ALTER TABLE production_bom_item DROP CONSTRAINT IF EXISTS uk_production_bom_material;
ALTER TABLE production_bom_item ADD CONSTRAINT uk_production_bom_item UNIQUE (bom_id, item_type, material_id);
