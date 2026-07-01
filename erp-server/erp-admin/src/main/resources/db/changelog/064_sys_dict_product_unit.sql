--liquibase formatted sql logicalFilePath:db/changelog/db.changelog-master.sql

--changeset erp:v1-4-3-add-product-unit-dict-001
INSERT INTO sys_dict_type (id, name, code, description, status)
SELECT gen_random_uuid(), '计量单位', 'product_unit', '产品/原料/物料通用计量单位', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'product_unit');

--changeset erp:v1-4-3-add-product-unit-dict-002
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '个', '个', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '个');

--changeset erp:v1-4-3-add-product-unit-dict-003
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '支', '支', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '支');

--changeset erp:v1-4-3-add-product-unit-dict-004
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '米', '米', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '米');

--changeset erp:v1-4-3-add-product-unit-dict-005
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '千克', '千克', 4, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '千克');

--changeset erp:v1-4-3-add-product-unit-dict-006
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '盒', '盒', 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '盒');

--changeset erp:v1-4-3-add-product-unit-dict-007
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '箱', '箱', 6, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '箱');

--changeset erp:v1-4-3-add-product-unit-dict-008
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '件', '件', 7, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '件');

--changeset erp:v1-4-3-add-product-unit-dict-009
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '套', '套', 8, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '套');

--changeset erp:v1-4-3-add-product-unit-dict-010
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '升', '升', 9, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '升');

--changeset erp:v1-4-3-add-product-unit-dict-011
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '吨', '吨', 10, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '吨');

--changeset erp:v1-4-3-add-product-unit-dict-012
INSERT INTO sys_dict_data (id, dict_type_code, label, value, sort_order, status)
SELECT gen_random_uuid(), 'product_unit', '卷', '卷', 11, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type_code = 'product_unit' AND value = '卷');
