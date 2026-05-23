CREATE TABLE IF NOT EXISTS material_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES material_category(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS supplier (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    contact_person VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    credit_rating SMALLINT,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS material (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES material_category(id),
    unit VARCHAR(20) NOT NULL,
    specifications VARCHAR(500),
    default_supplier_id UUID REFERENCES supplier(id),
    safety_stock NUMERIC(12,2) DEFAULT 0,
    lead_time_days INTEGER,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
