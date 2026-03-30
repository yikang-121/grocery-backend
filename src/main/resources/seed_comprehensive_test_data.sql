-- 1. DEEP CLEANUP (Remove all history for these SKUs to ensure Sync Parity)
DELETE FROM inventory_metrics WHERE sku_id IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004');
DELETE FROM order_items WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004'));
DELETE FROM orders WHERE order_no LIKE 'TEST-%';
DELETE FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004');

    -- 2. INSERT TEST PRODUCTS
    INSERT INTO product (sku, name, category, price, stock_quantity) VALUES
    ('VG-OAT-001', 'Test: Oat Milk (Momentum)', 'Dairy', 5.50, 0),
    ('VG-STW-002', 'Test: Fresh Strawberries (Decay)', 'Produce', 6.99, 0),
    ('VG-WAG-003', 'Test: Wagyu Beef (Volatility)', 'Meat', 85.00, 0),
    ('VG-TRK-004', 'Test: Whole Turkey (Seasonality)', 'Meat', 45.00, 0);

    -- 3. INSERT ORDER HISTORY (PRECISION TUNED)
    SET @oat_id = (SELECT id FROM product WHERE sku = 'VG-OAT-001');
    SET @stw_id = (SELECT id FROM product WHERE sku = 'VG-STW-002');
    SET @wag_id = (SELECT id FROM product WHERE sku = 'VG-WAG-003');
    SET @trk_id = (SELECT id FROM product WHERE sku = 'VG-TRK-004');

    -- A. OAT MILK: Massive Spike (AvgSales30d = 10, AvgSales3d = 30)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-OAT-1', 1, 1, 'PENDING', NOW());
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @oat_id, 'Oat Milk', 5.5, 30, 165);

    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-OAT-2', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @oat_id, 'Oat Milk', 5.5, 30, 165);

    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-OAT-3', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @oat_id, 'Oat Milk', 5.5, 30, 165);

    -- Fill background sales
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-OAT-BG', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 15 DAY));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @oat_id, 'Oat Milk', 5.5, 210, 1155);

    -- B. STRAWBERRIES: Steady (AvgSales30d = 2)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-STW-1', 1, 1, 'PENDING', NOW());
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @stw_id, 'Strawberries', 6.99, 60, 419.4);

    -- C. WAGYU: High Volatility (Sales today to keep Momentum at 0, plus legacy spikes)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-WAG-1', 1, 1, 'PENDING', NOW());
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @wag_id, 'Wagyu', 85, 15, 1275); -- Recent sale
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-WAG-2', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 15 DAY));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @wag_id, 'Wagyu', 85, 135, 11475); -- Legacy spikes

    -- D. TURKEY: History from last year (Seasonality Factor = 2.5)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-TRK-1', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 YEAR));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @trk_id, 'Turkey', 45, 300, 13500); -- Last year boom
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-TRK-2', 1, 1, 'PENDING', NOW());
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @trk_id, 'Turkey', 45, 10, 450); -- Current steady sale


    -- 4. INSERT FINAL CONFIGS (Force precision values into metrics table)
    INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor) VALUES
    ('VG-OAT-001', 0, 3, 7, 30, 1, 0.0, 30.0, 10.0, 2.0, 1.0),
    ('VG-STW-002', 0, 2, 3, 3, 1, 0.40, 2.0, 2.0, 0.5, 1.0),
    ('VG-WAG-003', 0, 5, 7, 14, 1, 0.0, 5.0, 5.0, 8.0, 1.0),
    ('VG-TRK-004', 0, 7, 14, 30, 1, 0.0, 10.0, 10.0, 2.0, 2.5);
