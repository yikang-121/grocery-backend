-- DELETE existing demo data if needed to avoid duplicates
DELETE FROM order_items WHERE product_id IN (SELECT id FROM product WHERE sku IN ('DEMO-STW-001', 'DEMO-HOT-002', 'DEMO-CLM-003'));
DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_no IN ('DEMO-ORD-101', 'DEMO-ORD-102', 'DEMO-ORD-HIST'));
DELETE FROM orders WHERE order_no IN ('DEMO-ORD-101', 'DEMO-ORD-102', 'DEMO-ORD-HIST');
DELETE FROM inventory_metrics WHERE sku_id IN ('DEMO-STW-001', 'DEMO-HOT-002', 'DEMO-CLM-003');
DELETE FROM product WHERE sku IN ('DEMO-STW-001', 'DEMO-HOT-002', 'DEMO-CLM-003');

-- 1. INSERT THE DEMO PRODUCTS
INSERT INTO product (sku, name, category, price, stock_quantity, created_at, updated_at) VALUES
('DEMO-STW-001', 'Demo: Fresh Organic Strawberries', 'Produce', 4.99, 0, NOW(), NOW()),
('DEMO-HOT-002', 'Demo: "Inferno" Hot Sauce', 'Condiments', 8.50, 0, NOW(), NOW()),
('DEMO-CLM-003', 'Demo: Seasonality Milk (Dec)', 'Dairy', 3.20, 0, NOW(), NOW());

-- 2. INSERT INVENTORY METRICS
-- Set MOQ=1 so we can see the exact calculated differences!
INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor) VALUES
('DEMO-STW-001', 0, 2, 3, 3, 1, 0.15, 2.0, 2.5, 0.5, 1.0),
('DEMO-HOT-002', 0, 5, 7, 365, 1, 0.001, 12.0, 2.0, 1.0, 1.0),
('DEMO-CLM-003', 0, 2, 3, 10, 1, 0.05, 5.0, 5.0, 1.2, 1.5);

-- 3. SEED ORDERS FOR MOMENTUM (HOT SAUCE) & SEASONALITY (MILK)
SET @hot_id = (SELECT id FROM product WHERE sku = 'DEMO-HOT-002');
SET @milk_id = (SELECT id FROM product WHERE sku = 'DEMO-CLM-003');

-- HOT SAUCE SPIKE (Yesterday/Today)
INSERT INTO orders (order_no, user_id, total_amount, status, created_at, updated_at) VALUES ('DEMO-ORD-101', 1, 50.0, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @oid1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (@oid1, @hot_id, 'Demo: "Inferno" Hot Sauce', 8.50, 30, 255.00);

-- MILK HISTORICAL SEASONALITY (Same month, Last Year)
-- We seed high volume in 2025-03 to create a > 1.0 seasonality factor
INSERT INTO orders (order_no, user_id, total_amount, status, created_at, updated_at) VALUES ('DEMO-ORD-HIST', 1, 100.0, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 YEAR), DATE_SUB(NOW(), INTERVAL 1 YEAR));
SET @oid_hist = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (@oid_hist, @milk_id, 'Demo: Seasonality Milk (Dec)', 3.20, 300, 960.00);
