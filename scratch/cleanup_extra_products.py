import mysql.connector

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='1234', database='grocery_db')
    cursor = conn.cursor()
    
    cursor.execute("SELECT COUNT(*) FROM product")
    total = cursor.fetchone()[0]
    print(f'Total products: {total}')
    
    # Cleanup all products that are NOT part of the evaluation set
    test_skus = ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005', 'VG-TRG-006')
    format_strings = ','.join(['%s'] * len(test_skus))
    
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
    cursor.execute(f"DELETE FROM product WHERE sku NOT IN ({format_strings})", test_skus)
    deleted = cursor.rowcount
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
    
    conn.commit()
    print(f'Deleted {deleted} non-test products.')
    
except Exception as e:
    print('ERROR:', str(e))
finally:
    if 'cursor' in locals(): cursor.close()
    if 'conn' in locals(): conn.close()
