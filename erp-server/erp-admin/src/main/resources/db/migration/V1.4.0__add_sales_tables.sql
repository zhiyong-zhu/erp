-- ============================================================
-- V1.4.0: 销售模块表结构
-- 包含: customer, sale_order, sale_order_item, sale_return,
--        sale_return_item, shipping_order, ecommerce_shop,
--        ecommerce_sku_mapping
-- ============================================================

-- 1. 客户表
CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    customer_type SMALLINT DEFAULT 1,
    contact_person VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(200),
    address VARCHAR(500),
    credit_limit NUMERIC(14,2) DEFAULT 0,
    payment_terms SMALLINT DEFAULT 1,
    sales_rep_id UUID,
    tax_number VARCHAR(50),
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_customer_name ON customer (name);
CREATE INDEX IF NOT EXISTS idx_customer_status ON customer (status);

-- 2. 销售订单主表
CREATE TABLE IF NOT EXISTS sale_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customer(id),
    customer_name VARCHAR(200) NOT NULL,
    order_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    platform_order_no VARCHAR(100),
    platform_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CONFIRM',
    total_amount NUMERIC(14,2) DEFAULT 0,
    discount_amount NUMERIC(14,2) DEFAULT 0,
    freight_amount NUMERIC(14,2) DEFAULT 0,
    payable_amount NUMERIC(14,2) DEFAULT 0,
    paid_amount NUMERIC(14,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    shipping_address JSONB,
    remark VARCHAR(500),
    ordered_at TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sale_order_customer ON sale_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_sale_order_status ON sale_order (status);
CREATE INDEX IF NOT EXISTS idx_sale_order_source ON sale_order (order_source);
CREATE INDEX IF NOT EXISTS idx_sale_order_platform ON sale_order (platform_order_no) WHERE platform_order_no IS NOT NULL;

-- 3. 销售订单明细表
CREATE TABLE IF NOT EXISTS sale_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_order_id UUID NOT NULL REFERENCES sale_order(id) ON DELETE CASCADE,
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    unit VARCHAR(20),
    quantity NUMERIC(12,2) NOT NULL,
    shipped_quantity NUMERIC(12,2) DEFAULT 0,
    unit_price NUMERIC(12,4),
    amount NUMERIC(14,2),
    remark VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sale_item_order ON sale_order_item (sale_order_id);

-- 4. 销售退货主表
CREATE TABLE IF NOT EXISTS sale_return (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_no VARCHAR(50) NOT NULL UNIQUE,
    sale_order_id UUID NOT NULL REFERENCES sale_order(id),
    sale_order_no VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW',
    total_amount NUMERIC(14,2) DEFAULT 0,
    reason VARCHAR(500),
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sale_return_order ON sale_return (sale_order_id);
CREATE INDEX IF NOT EXISTS idx_sale_return_status ON sale_return (status);

-- 5. 销售退货明细表
CREATE TABLE IF NOT EXISTS sale_return_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_return_id UUID NOT NULL REFERENCES sale_return(id) ON DELETE CASCADE,
    sale_order_item_id UUID,
    sku_id UUID,
    sku_code VARCHAR(50),
    product_name VARCHAR(200),
    quantity NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,4),
    return_amount NUMERIC(14,2),
    reason VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sale_return_item_return ON sale_return_item (sale_return_id);

-- 6. 发货/物流单表
CREATE TABLE IF NOT EXISTS shipping_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_order_id UUID NOT NULL REFERENCES sale_order(id),
    carrier_code VARCHAR(50),
    carrier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipped_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ,
    remark VARCHAR(500),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_shipping_order_sale ON shipping_order (sale_order_id);

-- 7. 电商店铺表
CREATE TABLE IF NOT EXISTS ecommerce_shop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    platform VARCHAR(20) NOT NULL,
    shop_name VARCHAR(200) NOT NULL,
    shop_id_on_platform VARCHAR(100) NOT NULL,
    access_token VARCHAR(500),
    refresh_token VARCHAR(500),
    token_expires_at TIMESTAMPTZ,
    sync_config JSONB,
    status SMALLINT DEFAULT 1,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ecommerce_shop_platform ON ecommerce_shop (platform);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ecommerce_shop_platform_id ON ecommerce_shop (platform, shop_id_on_platform);

-- 8. 电商 SKU 映射表
CREATE TABLE IF NOT EXISTS ecommerce_sku_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES ecommerce_shop(id),
    platform_sku_id VARCHAR(100) NOT NULL,
    platform_product_name VARCHAR(200),
    sku_id UUID,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_by UUID,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ecommerce_sku_mapping ON ecommerce_sku_mapping (shop_id, platform_sku_id);
