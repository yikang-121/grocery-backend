import mysql.connector

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='1234', database='grocery_db')
    cursor = conn.cursor()
    # Sync stock_quantity with batches
    cursor.execute("""
        UPDATE product p 
        SET stock_quantity = (
            SELECT COALESCE(SUM(available_quantity), 0) 
            FROM batch b 
            WHERE b.product_id = p.id
        );
    """)
    conn.commit()
    print('SUCCESS: Synchronized stock_quantity with batch data.')
except Exception as e:
    print('ERROR:', str(e))
finally:
    if 'cursor' in locals(): cursor.close()
    if 'conn' in locals(): conn.close()
