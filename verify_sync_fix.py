import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="1234",
        database="grocery_db"
    )
    cursor = conn.cursor(dictionary=True)
    
    # 1. Get real-time stock from product table for benchmark SKUs
    skus = ['VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004']
    query_p = "SELECT sku, stock_quantity FROM product WHERE sku IN (%s, %s, %s, %s)" % tuple(['%s']*4)
    cursor.execute(query_p, skus)
    product_stocks = {row['sku']: row['stock_quantity'] for row in cursor.fetchall()}
    
    # 2. Update inventory_metrics for these SKUs (Simulating the new Java logic)
    print("Simulating Sync for Benchmark SKUs...")
    for sku, qty in product_stocks.items():
        print(f"Updating {sku} to stock_quantity={qty}")
        update_query = "UPDATE inventory_metrics SET current_stock = %s WHERE sku_id = %s"
        cursor.execute(update_query, (qty, sku))
    
    conn.commit()
    print("Sync Simulation Complete.")

    # 3. Final verification
    print("\n--- FINAL METRICS STATUS ---")
    query_m = "SELECT sku_id, current_stock FROM inventory_metrics WHERE sku_id IN (%s, %s, %s, %s)" % tuple(['%s']*4)
    cursor.execute(query_m, skus)
    for row in cursor.fetchall():
        print(row)
        
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
