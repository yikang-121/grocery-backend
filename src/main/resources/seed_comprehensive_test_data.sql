DELETE FROM inventory_metrics WHERE sku_id IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006');
DELETE FROM order_items WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006'));
DELETE FROM batch WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006'));
DELETE FROM orders WHERE order_no LIKE 'TEST-%';
DELETE FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006');

    -- 2. INSERT TEST PRODUCTS
    INSERT INTO product (sku, name, category, price, stock_quantity, cost_price) VALUES
    ('VG-OAT-001', 'Test: Oat Milk (Momentum)', 'Dairy', 5.50, 0, 2.00),
    ('VG-STW-002', 'Test: Fresh Strawberries (Decay)', 'Produce', 6.99, 0, 3.00),
    ('VG-WAG-003', 'Test: Wagyu Beef (Volatility)', 'Meat', 85.00, 0, 45.00),
    ('VG-TRK-004', 'Test: Whole Turkey (Seasonality)', 'Meat', 45.00, 0, 20.00),
    ('VG-MNC-005', 'Mooncakes (Seasonality)', 'Bakery', 12.00, 0, 5.00),
    ('VG-TRG-006', 'Premium Salmon (Auto-Trigger)', 'Seafood', 35.00, 201, 15.00);

    -- 3. INSERT ORDER HISTORY (PRECISION TUNED)
    SET @oat_id = (SELECT id FROM product WHERE sku = 'VG-OAT-001');
    SET @stw_id = (SELECT id FROM product WHERE sku = 'VG-STW-002');
    SET @wag_id = (SELECT id FROM product WHERE sku = 'VG-WAG-003');
    SET @trk_id = (SELECT id FROM product WHERE sku = 'VG-TRK-004');
    SET @mnc_id = (SELECT id FROM product WHERE sku = 'VG-MNC-005');
    SET @trg_id = (SELECT id FROM product WHERE sku = 'VG-TRG-006');

    -- A. OAT MILK: Massive Spike (AvgSales30d = 10, AvgSales3d = 30)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-OAT-1', 1, 1, 'PENDING', NOW());
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @oat_id, 'Oat Milk', 5.5, 30, 165);

    -- D. TURKEY: History from last year (Seasonality Factor = 2.5)
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-TRK-1', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 YEAR));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @trk_id, 'Turkey', 45, 300, 13500);

    -- E. MOONCAKES: Historical Peak
    INSERT INTO orders (order_no, user_id, total_amount, status, created_at) VALUES ('TEST-MNC-1', 1, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 YEAR));
    INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES (LAST_INSERT_ID(), @mnc_id, 'Mooncakes', 12, 500, 6000);

    -- 4. INSERT FINAL CONFIGS (Force precision values into metrics table)
    INSERT INTO inventory_metrics (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor) VALUES
    ('VG-OAT-001', 0, 3, 7, 30, 1, 0.0, 30.0, 10.0, 2.0, 1.0),
    ('VG-STW-002', 0, 2, 3, 3, 1, 0.40, 2.0, 2.0, 0.5, 1.0),
    ('VG-WAG-003', 0, 5, 7, 14, 1, 0.0, 5.0, 5.0, 8.0, 1.0),
    ('VG-TRK-004', 0, 7, 14, 30, 1, 0.0, 10.0, 10.0, 2.0, 2.5),
    ('VG-MNC-005', 0, 3, 7, 30, 1, 0.0, 25.0, 25.0, 4.0, 1.35),
    ('VG-TRG-006', 201, 5, 7, 30, 1, 0.0, 10.0, 10.0, 10.0, 1.0);

    -- 5. SEED BATCHES
    INSERT INTO batch (product_id, batch_no, available_quantity, expiry_date, created_at, updated_at) 
    SELECT id, CONCAT('B-SEED-', sku), 
           CASE WHEN sku = 'VG-TRG-006' THEN 201 ELSE 100 END, 
           DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW()
    FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006');

    -- 6. SYNC AGGREGATE STOCK
    SET SQL_SAFE_UPDATES = 0;
    UPDATE product p SET stock_quantity = (SELECT COALESCE(SUM(available_quantity), 0) FROM batch b WHERE b.product_id = p.id);
    UPDATE inventory_metrics m SET current_stock = (SELECT stock_quantity FROM product p WHERE p.sku = m.sku_id);
    SET SQL_SAFE_UPDATES = 1;

