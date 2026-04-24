    -- V2: Accounting Module - Stock Movements, Purchase Orders
    -- Run this against grocery_db

    -- 1. Stock Movement Audit Trail
    CREATE TABLE IF NOT EXISTS stock_movements (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        product_id BIGINT NOT NULL,
        batch_id BIGINT NULL,
        movement_type VARCHAR(20) NOT NULL COMMENT 'STOCK_IN, STOCK_OUT, SPOILAGE, RESTOCK, ORDER_DEDUCT, CANCEL_RETURN',
        quantity INT NOT NULL,
        reference_type VARCHAR(30) NULL COMMENT 'ORDER, BATCH, SPOILAGE_LOG, PURCHASE_ORDER, CSV_UPLOAD',
        reference_id BIGINT NULL,
        notes VARCHAR(500) NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_sm_product (product_id),
        INDEX idx_sm_type (movement_type),
        INDEX idx_sm_created (created_at)
    );

    -- 2. Purchase Orders (supplier restocking)
    CREATE TABLE IF NOT EXISTS purchase_orders (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        po_number VARCHAR(20) NOT NULL UNIQUE,
        supplier_name VARCHAR(100) NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT 'PENDING_APPROVAL, APPROVED, RECEIVED, CANCELLED',
        total_cost DECIMAL(12,2) DEFAULT 0.00,
        tax_amount DECIMAL(12,2) DEFAULT 0.00,
        notes TEXT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        approved_at TIMESTAMP NULL,
        received_at TIMESTAMP NULL,
        INDEX idx_po_status (status),
        INDEX idx_po_created (created_at)
    );

    -- 3. Purchase Order Line Items
    CREATE TABLE IF NOT EXISTS purchase_order_items (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        purchase_order_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        sku VARCHAR(50) NULL,
        product_name VARCHAR(200) NULL,
        quantity_ordered INT NOT NULL DEFAULT 0,
        quantity_received INT NOT NULL DEFAULT 0,
        unit_cost DECIMAL(10,2) DEFAULT 0.00,
        line_total DECIMAL(12,2) DEFAULT 0.00,
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
        INDEX idx_poi_po (purchase_order_id),
        INDEX idx_poi_product (product_id)
    );
