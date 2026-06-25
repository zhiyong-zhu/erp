--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-8-1-create-inventory-warehouse-001
CREATE TABLE IF NOT EXISTS inventory_warehouse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    manager_name VARCHAR(100),
    phone VARCHAR(50),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

--changeset erp:v1-8-1-create-inventory-warehouse-002
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_status ON inventory_warehouse(status);

--changeset erp:v1-8-1-create-inventory-location-001
CREATE TABLE IF NOT EXISTS inventory_location (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES inventory_warehouse(id),
    warehouse_code VARCHAR(50) NOT NULL,
    warehouse_name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    area_code VARCHAR(50),
    area_name VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT uk_inventory_location_warehouse_code UNIQUE (warehouse_id, code)
);

--changeset erp:v1-8-1-create-inventory-location-002
CREATE INDEX IF NOT EXISTS idx_inventory_location_warehouse_id ON inventory_location(warehouse_id);

--changeset erp:v1-8-1-create-inventory-location-003
CREATE INDEX IF NOT EXISTS idx_inventory_location_status ON inventory_location(status);

--changeset erp:v1-8-1-seed-default-inventory-warehouse-001
INSERT INTO inventory_warehouse (code, name, address, sort_order, status, remark)
SELECT 'MAIN', '主仓', '默认仓库', 0, 1, '系统默认仓库'
WHERE NOT EXISTS (SELECT 1 FROM inventory_warehouse WHERE code = 'MAIN');

--changeset erp:v1-8-1-seed-default-inventory-location-001
INSERT INTO inventory_location (warehouse_id, warehouse_code, warehouse_name, code, name, sort_order, status, remark)
SELECT w.id, w.code, w.name, 'DEFAULT', '默认库位', 0, 1, '系统默认库位'
FROM inventory_warehouse w
WHERE w.code = 'MAIN'
  AND NOT EXISTS (
      SELECT 1 FROM inventory_location l WHERE l.warehouse_id = w.id AND l.code = 'DEFAULT'
  );
