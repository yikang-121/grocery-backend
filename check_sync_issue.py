import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="1234",
        database="grocery_db"
    )
    cursor = conn.cursor(dictionary=True)
    
    skus = ['VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004']
    
    print("--- PRODUCT TABLE STATUS ---")
    query_p = "SELECT sku, name, stock_quantity FROM product WHERE sku IN (%s, %s, %s, %s)" % tuple(['%s']*4)
    cursor.execute(query_p, skus)
    for row in cursor.fetchall():
        print(row)
        
    print("\n--- INVENTORY_METRICS TABLE STATUS ---")
    query_m = "SELECT sku_id, current_stock FROM inventory_metrics WHERE sku_id IN (%s, %s, %s, %s)" % tuple(['%s']*4)
    cursor.execute(query_m, skus)
    for row in cursor.fetchall():
        print(row)
        
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
