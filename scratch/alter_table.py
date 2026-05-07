import mysql.connector

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='1234', database='grocery_db')
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE product ADD COLUMN stock_quantity INT DEFAULT 0;")
    conn.commit()
    print('SUCCESS')
except Exception as e:
    print('ERROR:', str(e))
finally:
    if 'cursor' in locals(): cursor.close()
    if 'conn' in locals(): conn.close()
