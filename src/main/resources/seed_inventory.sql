-- c:\Users\User\FYP\grocery-backend\src\main\resources\seed_inventory.sql

-- 1. Create or ensure Product table exists
-- (Assuming standard generation, but adding explicit records here for the simulator)
INSERT INTO product (name, category, price, stock_quantity, sku)
VALUES 
('Fresh Strawberries 250g', 'Produce', 12.50, 50, 'STRAW-001'),
('Canned Baked Beans 400g', 'Canned Goods', 3.20, 100, 'BEAN-001'),
('Viral Spicy Hot Sauce', 'Condiments', 8.90, 20, 'SAUCE-001')
ON DUPLICATE KEY UPDATE stock_quantity=VALUES(stock_quantity);

-- 2. Create the Inventory Metrics Table
CREATE TABLE IF NOT EXISTS inventory_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id VARCHAR(255) NOT NULL,
    current_stock INT,
    lead_time_days INT,
    review_period_days INT,
    shelf_life_days INT,
    supplier_moq INT,
    waste_lambda DOUBLE,
    avg_sales_3d DOUBLE,
    avg_sales_30d DOUBLE,
    std_dev_30d DOUBLE,
    UNIQUE KEY (sku_id)
);

-- 3. Insert per-product inventory constraints
-- These static fields define each product's restocking characteristics.
-- Dynamic fields (avg_sales_3d, avg_sales_30d, std_dev_30d) are populated
-- by running sync-metrics AFTER seeding orders from seed_orders.sql.

-- 3a. Highly perishable, high-volatility item (Fresh Strawberries)
--     Short lead time (1 day), daily review, 3-day shelf life, high waste risk
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('STRAW-001', 50, 1, 1, 3, 10, 0.15, 0, 0, 0)
ON DUPLICATE KEY UPDATE current_stock=50, lead_time_days=1, review_period_days=1, shelf_life_days=3, supplier_moq=10, waste_lambda=0.15;

-- 3b. Stable, non-perishable item (Canned Beans)
--     Weekly lead time, weekly review, 1-year shelf life, bulk MOQ, negligible waste
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('BEAN-001', 100, 7, 7, 365, 50, 0.001, 0, 0, 0)
ON DUPLICATE KEY UPDATE current_stock=100, lead_time_days=7, review_period_days=7, shelf_life_days=365, supplier_moq=50, waste_lambda=0.001;

-- 3c. Trending item with demand spike potential (Viral Hot Sauce)
--     3-day lead time, 3-day review, 6-month shelf life, moderate MOQ
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('SAUCE-001', 20, 3, 3, 180, 24, 0.005, 0, 0, 0)
ON DUPLICATE KEY UPDATE current_stock=20, lead_time_days=3, review_period_days=3, shelf_life_days=180, supplier_moq=24, waste_lambda=0.005;
