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

-- 3. Insert specific algorithmic mock constraints for these products
-- 3a. Highly perishable, high-volatility item (Fresh Strawberries)
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('STRAW-001', 50, 2, 1, 3, 10, 0.1, 40.0, 45.0, 15.0)
ON DUPLICATE KEY UPDATE current_stock=50;

-- 3b. Stable, non-perishable item (Canned Beans)
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('BEAN-001', 100, 7, 7, 365, 50, 0.001, 15.0, 16.0, 2.0)
ON DUPLICATE KEY UPDATE current_stock=100;

-- 3c. Trending item experiencing a sudden demand spike (Viral Hot Sauce)
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d)
VALUES ('SAUCE-001', 20, 5, 2, 180, 100, 0.005, 100.0, 20.0, 5.0)
ON DUPLICATE KEY UPDATE current_stock=20;
