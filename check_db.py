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
    query = "SELECT * FROM inventory_metrics WHERE sku_id IN (%s, %s, %s, %s)" % tuple(['%s']*4)
    cursor.execute(query, skus)
    rows = cursor.fetchall()
    
    print("--- CURRENT DATABASE METRICS ---")
    for row in rows:
        print(row)
        
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
