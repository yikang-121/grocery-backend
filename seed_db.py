import mysql.connector

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='1234', database='grocery_db')
    cursor = conn.cursor()
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
    cursor.execute("DELETE FROM inventory_metrics WHERE sku_id IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005');")
    cursor.execute("DELETE FROM spoilage_log WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005'));")
    cursor.execute("DELETE FROM order_items WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005'));")
    cursor.execute("DELETE FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005');")
    
    cursor.execute("""
    INSERT INTO product (sku, name, category, price, cost_price, stock_quantity) VALUES
    ('VG-OAT-001', 'Organic Oats (Momentum)', 'Dairy', 5.50, 2.00, 10),
    ('VG-STW-002', 'Fresh Strawberries (Perishability)', 'Produce', 6.99, 3.00, 0),
    ('VG-WAG-003', 'Wagyu Beef (Zero Sales)', 'Meat', 85.00, 45.00, 5),
    ('VG-TRK-004', 'Turkey Mince (Decay)', 'Meat', 45.00, 20.00, 0),
    ('VG-MNC-005', 'Mooncakes (Seasonality)', 'Bakery', 12.00, 5.00, 10);
    """)
    
    cursor.execute("""
    INSERT INTO inventory_metrics 
    (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, case_size, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor, incoming_stock) 
    VALUES
    ('VG-OAT-001', 10, 3, 7, 90, 1, 1, 0.01, 50.0, 20.0, 5.0, 1.0, 0),
    ('VG-STW-002', 0,  5, 2,  3, 1, 1, 0.00, 20.0, 20.0, 2.0, 1.0, 0),
    ('VG-WAG-003', 5,  2, 1, 30,10, 6, 0.05,  0.0,  0.0, 0.0, 1.0, 0),
    ('VG-TRK-004', 0,  2, 2,  5, 1, 1, 0.20, 30.0, 30.0, 5.0, 1.0, 0),
    ('VG-MNC-005', 10, 3, 7, 30, 1, 1, 0.01, 25.0, 25.0, 4.0, 1.5, 0);
    """)
    
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    conn.commit()
    print('SUCCESS')
except Exception as e:
    print('ERROR:', str(e))
finally:
    if 'cursor' in locals(): cursor.close()
    if 'conn' in locals(): conn.close()
