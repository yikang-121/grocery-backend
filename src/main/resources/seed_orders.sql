-- seed_orders.sql
-- Seeds ~60 orders across the last 30 days for the 3 core products
-- Run AFTER seed_inventory.sql so products exist
-- All orders use user_id = 1, status = 'COMPLETED'

-- ============================================================
-- STEP 1: Resolve product IDs from SKU
-- ============================================================
SET @straw_id  = (SELECT id FROM product WHERE sku = 'STRAW-001' LIMIT 1);
SET @bean_id   = (SELECT id FROM product WHERE sku = 'BEAN-001'  LIMIT 1);
SET @sauce_id  = (SELECT id FROM product WHERE sku = 'SAUCE-001' LIMIT 1);

-- ============================================================
-- STEP 2: Insert Orders (60 orders across 30 days)
-- Pattern:
--   - Days 1-27 (older): ~2 orders/day, normal demand
--   - Days 28-30 (recent): HOT SAUCE spike + continued patterns
-- ============================================================
-- We'll use order_no = 'SEED-001' .. 'SEED-060'

INSERT INTO orders (order_no, user_id, subtotal, shipping_fee, discount, total_amount, status, payment_method, shipping_address, notes, created_at, updated_at) VALUES
-- Day -30 (30 days ago)
('SEED-001', 1, 37.60, 5.00, 0, 42.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 9 HOUR),
('SEED-002', 1, 22.10, 5.00, 0, 27.10, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 15 HOUR),
-- Day -29
('SEED-003', 1, 28.40, 5.00, 0, 33.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 10 HOUR),
('SEED-004', 1, 15.70, 5.00, 0, 20.70, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 29 DAY) + INTERVAL 16 HOUR),
-- Day -28
('SEED-005', 1, 41.30, 5.00, 0, 46.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 8 HOUR),
('SEED-006', 1, 19.20, 5.00, 0, 24.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 14 HOUR),
-- Day -27
('SEED-007', 1, 33.90, 5.00, 0, 38.90, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 11 HOUR),
('SEED-008', 1, 25.00, 5.00, 0, 30.00, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 27 DAY) + INTERVAL 17 HOUR),
-- Day -26
('SEED-009', 1, 50.00, 5.00, 0, 55.00, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 9 HOUR),
('SEED-010', 1, 12.50, 5.00, 0, 17.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 26 DAY) + INTERVAL 13 HOUR),
-- Day -25
('SEED-011', 1, 31.40, 5.00, 0, 36.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 10 HOUR),
('SEED-012', 1, 21.80, 5.00, 0, 26.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 16 HOUR),
-- Day -24
('SEED-013', 1, 44.50, 5.00, 0, 49.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 9 HOUR),
('SEED-014', 1, 18.90, 5.00, 0, 23.90, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 24 DAY) + INTERVAL 14 HOUR),
-- Day -23
('SEED-015', 1, 27.50, 5.00, 0, 32.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 11 HOUR),
('SEED-016', 1, 35.60, 5.00, 0, 40.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 18 HOUR, DATE_SUB(NOW(), INTERVAL 23 DAY) + INTERVAL 18 HOUR),
-- Day -22
('SEED-017', 1, 15.00, 5.00, 0, 20.00, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 10 HOUR),
('SEED-018', 1, 40.20, 5.00, 0, 45.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 15 HOUR),
-- Day -21
('SEED-019', 1, 29.80, 5.00, 0, 34.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 9 HOUR),
('SEED-020', 1, 22.50, 5.00, 0, 27.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 21 DAY) + INTERVAL 14 HOUR),
-- Day -20
('SEED-021', 1, 47.30, 5.00, 0, 52.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 10 HOUR),
('SEED-022', 1, 16.40, 5.00, 0, 21.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 17 HOUR),
-- Day -19
('SEED-023', 1, 33.20, 5.00, 0, 38.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 8 HOUR),
('SEED-024', 1, 24.60, 5.00, 0, 29.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 13 HOUR),
-- Day -18
('SEED-025', 1, 38.80, 5.00, 0, 43.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 10 HOUR),
('SEED-026', 1, 19.50, 5.00, 0, 24.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 16 HOUR),
-- Day -17
('SEED-027', 1, 26.70, 5.00, 0, 31.70, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 11 HOUR),
('SEED-028', 1, 42.10, 5.00, 0, 47.10, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 17 DAY) + INTERVAL 15 HOUR),
-- Day -16
('SEED-029', 1, 14.80, 5.00, 0, 19.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 9 HOUR),
('SEED-030', 1, 36.90, 5.00, 0, 41.90, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 14 HOUR),
-- Day -15
('SEED-031', 1, 30.50, 5.00, 0, 35.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 10 HOUR),
('SEED-032', 1, 23.40, 5.00, 0, 28.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 17 HOUR),
-- Day -14
('SEED-033', 1, 45.60, 5.00, 0, 50.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 8 HOUR),
('SEED-034', 1, 17.30, 5.00, 0, 22.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 13 HOUR),
-- Day -13
('SEED-035', 1, 32.80, 5.00, 0, 37.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 10 HOUR),
('SEED-036', 1, 20.90, 5.00, 0, 25.90, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 13 DAY) + INTERVAL 16 HOUR),
-- Day -12
('SEED-037', 1, 39.40, 5.00, 0, 44.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 9 HOUR),
('SEED-038', 1, 25.70, 5.00, 0, 30.70, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 15 HOUR),
-- Day -11
('SEED-039', 1, 28.60, 5.00, 0, 33.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 11 HOUR),
('SEED-040', 1, 43.20, 5.00, 0, 48.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 14 HOUR),
-- Day -10
('SEED-041', 1, 16.80, 5.00, 0, 21.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 10 HOUR),
('SEED-042', 1, 34.50, 5.00, 0, 39.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 17 HOUR),
-- Day -9
('SEED-043', 1, 27.90, 5.00, 0, 32.90, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 9 HOUR),
('SEED-044', 1, 21.30, 5.00, 0, 26.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 13 HOUR),
-- Day -8
('SEED-045', 1, 46.70, 5.00, 0, 51.70, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 10 HOUR),
('SEED-046', 1, 18.40, 5.00, 0, 23.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 15 HOUR),
-- Day -7
('SEED-047', 1, 35.10, 5.00, 0, 40.10, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 11 HOUR),
('SEED-048', 1, 23.80, 5.00, 0, 28.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 16 HOUR),
-- Day -6
('SEED-049', 1, 29.40, 5.00, 0, 34.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 9 HOUR),
('SEED-050', 1, 41.60, 5.00, 0, 46.60, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 14 HOUR),
-- Day -5
('SEED-051', 1, 20.50, 5.00, 0, 25.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 10 HOUR),
('SEED-052', 1, 37.20, 5.00, 0, 42.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR),
-- Day -4
('SEED-053', 1, 26.30, 5.00, 0, 31.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 8 HOUR),
('SEED-054', 1, 43.80, 5.00, 0, 48.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 13 HOUR),
-- Day -3 (TRENDING STARTS: Hot Sauce demand spikes)
('SEED-055', 1, 98.70, 5.00, 0, 103.70, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 9 HOUR),
('SEED-056', 1, 115.30, 5.00, 0, 120.30, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR),
-- Day -2 (Hot Sauce demand continues high)
('SEED-057', 1, 107.50, 5.00, 0, 112.50, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 HOUR),
('SEED-058', 1, 89.20, 5.00, 0, 94.20, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 16 HOUR),
-- Day -1 (Yesterday - Hot Sauce still spiking)
('SEED-059', 1, 121.40, 5.00, 0, 126.40, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 9 HOUR),
('SEED-060', 1, 95.80, 5.00, 0, 100.80, 'COMPLETED', 'CARD', '{"line1":"123 Test St"}', 'seed', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR)

ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ============================================================
-- STEP 3: Insert Order Items
-- Demand patterns:
--   Strawberries: High & volatile (5-40 per order, erratic)
--   Canned Beans: Stable (~12-18 per order, consistent)
--   Hot Sauce:    Low for 27 days (~2-5), SPIKE last 3 days (~25-45)
-- ============================================================

-- Helper: get the order IDs
SET @o01 = (SELECT id FROM orders WHERE order_no = 'SEED-001');
SET @o02 = (SELECT id FROM orders WHERE order_no = 'SEED-002');
SET @o03 = (SELECT id FROM orders WHERE order_no = 'SEED-003');
SET @o04 = (SELECT id FROM orders WHERE order_no = 'SEED-004');
SET @o05 = (SELECT id FROM orders WHERE order_no = 'SEED-005');
SET @o06 = (SELECT id FROM orders WHERE order_no = 'SEED-006');
SET @o07 = (SELECT id FROM orders WHERE order_no = 'SEED-007');
SET @o08 = (SELECT id FROM orders WHERE order_no = 'SEED-008');
SET @o09 = (SELECT id FROM orders WHERE order_no = 'SEED-009');
SET @o10 = (SELECT id FROM orders WHERE order_no = 'SEED-010');
SET @o11 = (SELECT id FROM orders WHERE order_no = 'SEED-011');
SET @o12 = (SELECT id FROM orders WHERE order_no = 'SEED-012');
SET @o13 = (SELECT id FROM orders WHERE order_no = 'SEED-013');
SET @o14 = (SELECT id FROM orders WHERE order_no = 'SEED-014');
SET @o15 = (SELECT id FROM orders WHERE order_no = 'SEED-015');
SET @o16 = (SELECT id FROM orders WHERE order_no = 'SEED-016');
SET @o17 = (SELECT id FROM orders WHERE order_no = 'SEED-017');
SET @o18 = (SELECT id FROM orders WHERE order_no = 'SEED-018');
SET @o19 = (SELECT id FROM orders WHERE order_no = 'SEED-019');
SET @o20 = (SELECT id FROM orders WHERE order_no = 'SEED-020');
SET @o21 = (SELECT id FROM orders WHERE order_no = 'SEED-021');
SET @o22 = (SELECT id FROM orders WHERE order_no = 'SEED-022');
SET @o23 = (SELECT id FROM orders WHERE order_no = 'SEED-023');
SET @o24 = (SELECT id FROM orders WHERE order_no = 'SEED-024');
SET @o25 = (SELECT id FROM orders WHERE order_no = 'SEED-025');
SET @o26 = (SELECT id FROM orders WHERE order_no = 'SEED-026');
SET @o27 = (SELECT id FROM orders WHERE order_no = 'SEED-027');
SET @o28 = (SELECT id FROM orders WHERE order_no = 'SEED-028');
SET @o29 = (SELECT id FROM orders WHERE order_no = 'SEED-029');
SET @o30 = (SELECT id FROM orders WHERE order_no = 'SEED-030');
SET @o31 = (SELECT id FROM orders WHERE order_no = 'SEED-031');
SET @o32 = (SELECT id FROM orders WHERE order_no = 'SEED-032');
SET @o33 = (SELECT id FROM orders WHERE order_no = 'SEED-033');
SET @o34 = (SELECT id FROM orders WHERE order_no = 'SEED-034');
SET @o35 = (SELECT id FROM orders WHERE order_no = 'SEED-035');
SET @o36 = (SELECT id FROM orders WHERE order_no = 'SEED-036');
SET @o37 = (SELECT id FROM orders WHERE order_no = 'SEED-037');
SET @o38 = (SELECT id FROM orders WHERE order_no = 'SEED-038');
SET @o39 = (SELECT id FROM orders WHERE order_no = 'SEED-039');
SET @o40 = (SELECT id FROM orders WHERE order_no = 'SEED-040');
SET @o41 = (SELECT id FROM orders WHERE order_no = 'SEED-041');
SET @o42 = (SELECT id FROM orders WHERE order_no = 'SEED-042');
SET @o43 = (SELECT id FROM orders WHERE order_no = 'SEED-043');
SET @o44 = (SELECT id FROM orders WHERE order_no = 'SEED-044');
SET @o45 = (SELECT id FROM orders WHERE order_no = 'SEED-045');
SET @o46 = (SELECT id FROM orders WHERE order_no = 'SEED-046');
SET @o47 = (SELECT id FROM orders WHERE order_no = 'SEED-047');
SET @o48 = (SELECT id FROM orders WHERE order_no = 'SEED-048');
SET @o49 = (SELECT id FROM orders WHERE order_no = 'SEED-049');
SET @o50 = (SELECT id FROM orders WHERE order_no = 'SEED-050');
SET @o51 = (SELECT id FROM orders WHERE order_no = 'SEED-051');
SET @o52 = (SELECT id FROM orders WHERE order_no = 'SEED-052');
SET @o53 = (SELECT id FROM orders WHERE order_no = 'SEED-053');
SET @o54 = (SELECT id FROM orders WHERE order_no = 'SEED-054');
SET @o55 = (SELECT id FROM orders WHERE order_no = 'SEED-055');
SET @o56 = (SELECT id FROM orders WHERE order_no = 'SEED-056');
SET @o57 = (SELECT id FROM orders WHERE order_no = 'SEED-057');
SET @o58 = (SELECT id FROM orders WHERE order_no = 'SEED-058');
SET @o59 = (SELECT id FROM orders WHERE order_no = 'SEED-059');
SET @o60 = (SELECT id FROM orders WHERE order_no = 'SEED-060');

-- ============================================================
-- Order Items
-- Each order gets 1-3 product lines
-- ============================================================

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total) VALUES
-- Day -30 (Order 1-2): Strawberry volatile, Bean stable, Sauce low
(@o01, @straw_id, 'Fresh Strawberries 250g', 12.50, 25, 312.50),
(@o01, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o01, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),
(@o02, @straw_id, 'Fresh Strawberries 250g', 12.50, 8, 100.00),
(@o02, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),

-- Day -29
(@o03, @straw_id, 'Fresh Strawberries 250g', 12.50, 30, 375.00),
(@o03, @bean_id,  'Canned Baked Beans 400g', 3.20, 12, 38.40),
(@o04, @straw_id, 'Fresh Strawberries 250g', 12.50, 5, 62.50),
(@o04, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),

-- Day -28
(@o05, @straw_id, 'Fresh Strawberries 250g', 12.50, 35, 437.50),
(@o05, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o05, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),
(@o06, @straw_id, 'Fresh Strawberries 250g', 12.50, 10, 125.00),
(@o06, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),

-- Day -27
(@o07, @straw_id, 'Fresh Strawberries 250g', 12.50, 22, 275.00),
(@o07, @bean_id,  'Canned Baked Beans 400g', 3.20, 17, 54.40),
(@o08, @straw_id, 'Fresh Strawberries 250g', 12.50, 15, 187.50),
(@o08, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),

-- Day -26
(@o09, @straw_id, 'Fresh Strawberries 250g', 12.50, 40, 500.00),
(@o09, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o09, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 5, 44.50),
(@o10, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),

-- Day -25
(@o11, @straw_id, 'Fresh Strawberries 250g', 12.50, 18, 225.00),
(@o11, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o11, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),
(@o12, @straw_id, 'Fresh Strawberries 250g', 12.50, 12, 150.00),
(@o12, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),

-- Day -24
(@o13, @straw_id, 'Fresh Strawberries 250g', 12.50, 38, 475.00),
(@o13, @bean_id,  'Canned Baked Beans 400g', 3.20, 18, 57.60),
(@o14, @straw_id, 'Fresh Strawberries 250g', 12.50, 7, 87.50),
(@o14, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),

-- Day -23
(@o15, @straw_id, 'Fresh Strawberries 250g', 12.50, 20, 250.00),
(@o15, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o16, @straw_id, 'Fresh Strawberries 250g', 12.50, 28, 350.00),
(@o16, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o16, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),

-- Day -22
(@o17, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o17, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),
(@o18, @straw_id, 'Fresh Strawberries 250g', 12.50, 32, 400.00),
(@o18, @bean_id,  'Canned Baked Beans 400g', 3.20, 12, 38.40),

-- Day -21
(@o19, @straw_id, 'Fresh Strawberries 250g', 12.50, 15, 187.50),
(@o19, @bean_id,  'Canned Baked Beans 400g', 3.20, 17, 54.40),
(@o19, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),
(@o20, @straw_id, 'Fresh Strawberries 250g', 12.50, 22, 275.00),
(@o20, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),

-- Day -20
(@o21, @straw_id, 'Fresh Strawberries 250g', 12.50, 36, 450.00),
(@o21, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o21, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 5, 44.50),
(@o22, @straw_id, 'Fresh Strawberries 250g', 12.50, 6, 75.00),
(@o22, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),

-- Day -19
(@o23, @straw_id, 'Fresh Strawberries 250g', 12.50, 28, 350.00),
(@o23, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o24, @straw_id, 'Fresh Strawberries 250g', 12.50, 14, 175.00),
(@o24, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),

-- Day -18
(@o25, @straw_id, 'Fresh Strawberries 250g', 12.50, 33, 412.50),
(@o25, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o25, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),
(@o26, @straw_id, 'Fresh Strawberries 250g', 12.50, 9, 112.50),
(@o26, @bean_id,  'Canned Baked Beans 400g', 3.20, 18, 57.60),

-- Day -17
(@o27, @straw_id, 'Fresh Strawberries 250g', 12.50, 19, 237.50),
(@o27, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o28, @straw_id, 'Fresh Strawberries 250g', 12.50, 35, 437.50),
(@o28, @bean_id,  'Canned Baked Beans 400g', 3.20, 12, 38.40),
(@o28, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),

-- Day -16
(@o29, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o29, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),
(@o30, @straw_id, 'Fresh Strawberries 250g', 12.50, 26, 325.00),
(@o30, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),

-- Day -15
(@o31, @straw_id, 'Fresh Strawberries 250g', 12.50, 20, 250.00),
(@o31, @bean_id,  'Canned Baked Beans 400g', 3.20, 17, 54.40),
(@o31, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),
(@o32, @straw_id, 'Fresh Strawberries 250g', 12.50, 11, 137.50),
(@o32, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),

-- Day -14
(@o33, @straw_id, 'Fresh Strawberries 250g', 12.50, 37, 462.50),
(@o33, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o33, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),
(@o34, @straw_id, 'Fresh Strawberries 250g', 12.50, 8, 100.00),
(@o34, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),

-- Day -13
(@o35, @straw_id, 'Fresh Strawberries 250g', 12.50, 24, 300.00),
(@o35, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o36, @straw_id, 'Fresh Strawberries 250g', 12.50, 16, 200.00),
(@o36, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),

-- Day -12
(@o37, @straw_id, 'Fresh Strawberries 250g', 12.50, 30, 375.00),
(@o37, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o37, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 5, 44.50),
(@o38, @straw_id, 'Fresh Strawberries 250g', 12.50, 12, 150.00),
(@o38, @bean_id,  'Canned Baked Beans 400g', 3.20, 18, 57.60),

-- Day -11
(@o39, @straw_id, 'Fresh Strawberries 250g', 12.50, 21, 262.50),
(@o39, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o40, @straw_id, 'Fresh Strawberries 250g', 12.50, 34, 425.00),
(@o40, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),
(@o40, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),

-- Day -10
(@o41, @straw_id, 'Fresh Strawberries 250g', 12.50, 10, 125.00),
(@o41, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o42, @straw_id, 'Fresh Strawberries 250g', 12.50, 27, 337.50),
(@o42, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o42, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),

-- Day -9
(@o43, @straw_id, 'Fresh Strawberries 250g', 12.50, 18, 225.00),
(@o43, @bean_id,  'Canned Baked Beans 400g', 3.20, 17, 54.40),
(@o43, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),
(@o44, @straw_id, 'Fresh Strawberries 250g', 12.50, 13, 162.50),
(@o44, @bean_id,  'Canned Baked Beans 400g', 3.20, 12, 38.40),

-- Day -8
(@o45, @straw_id, 'Fresh Strawberries 250g', 12.50, 38, 475.00),
(@o45, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o46, @straw_id, 'Fresh Strawberries 250g', 12.50, 7, 87.50),
(@o46, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),

-- Day -7
(@o47, @straw_id, 'Fresh Strawberries 250g', 12.50, 23, 287.50),
(@o47, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o47, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 4, 35.60),
(@o48, @straw_id, 'Fresh Strawberries 250g', 12.50, 16, 200.00),
(@o48, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),

-- Day -6
(@o49, @straw_id, 'Fresh Strawberries 250g', 12.50, 29, 362.50),
(@o49, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),
(@o50, @straw_id, 'Fresh Strawberries 250g', 12.50, 14, 175.00),
(@o50, @bean_id,  'Canned Baked Beans 400g', 3.20, 18, 57.60),
(@o50, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),

-- Day -5
(@o51, @straw_id, 'Fresh Strawberries 250g', 12.50, 20, 250.00),
(@o51, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o51, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 2, 17.80),
(@o52, @straw_id, 'Fresh Strawberries 250g', 12.50, 31, 387.50),
(@o52, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),

-- Day -4
(@o53, @straw_id, 'Fresh Strawberries 250g', 12.50, 17, 212.50),
(@o53, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o53, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 3, 26.70),
(@o54, @straw_id, 'Fresh Strawberries 250g', 12.50, 25, 312.50),
(@o54, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),

-- ============================================================
-- Day -3: HOT SAUCE DEMAND SPIKE BEGINS
-- Strawberries still volatile, Beans still stable
-- ============================================================
(@o55, @straw_id, 'Fresh Strawberries 250g', 12.50, 22, 275.00),
(@o55, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o55, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 30, 267.00),
(@o56, @straw_id, 'Fresh Strawberries 250g', 12.50, 33, 412.50),
(@o56, @bean_id,  'Canned Baked Beans 400g', 3.20, 17, 54.40),
(@o56, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 35, 311.50),

-- Day -2: Hot Sauce continues trending
(@o57, @straw_id, 'Fresh Strawberries 250g', 12.50, 19, 237.50),
(@o57, @bean_id,  'Canned Baked Beans 400g', 3.20, 15, 48.00),
(@o57, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 40, 356.00),
(@o58, @straw_id, 'Fresh Strawberries 250g', 12.50, 28, 350.00),
(@o58, @bean_id,  'Canned Baked Beans 400g', 3.20, 13, 41.60),
(@o58, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 25, 222.50),

-- Day -1 (Yesterday): Hot Sauce still spiking
(@o59, @straw_id, 'Fresh Strawberries 250g', 12.50, 35, 437.50),
(@o59, @bean_id,  'Canned Baked Beans 400g', 3.20, 16, 51.20),
(@o59, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 45, 400.50),
(@o60, @straw_id, 'Fresh Strawberries 250g', 12.50, 12, 150.00),
(@o60, @bean_id,  'Canned Baked Beans 400g', 3.20, 14, 44.80),
(@o60, @sauce_id, 'Viral Spicy Hot Sauce',   8.90, 35, 311.50);

-- ============================================================
-- SUMMARY OF SEEDED DEMAND PATTERNS:
-- Strawberries: ~5-40 units/order, high daily variation (volatile perishable)
-- Canned Beans: ~12-18 units/order, stable across all days
-- Hot Sauce:    ~2-5 units/order for days 4-30, then 25-45 units/order for last 3 days (trending spike)
-- ============================================================
