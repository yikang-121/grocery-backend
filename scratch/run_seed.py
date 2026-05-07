import mysql.connector
import os

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='1234', database='grocery_db')
    cursor = conn.cursor()
    
    with open('src/main/resources/seed_comprehensive_test_data.sql', 'r') as f:
        sql = f.read()
    
    # split by semicolon but ignore inside strings if possible
    # simplest for this script is to split by ';'
    commands = sql.split(';')
    
    for command in commands:
        if command.strip():
            cursor.execute(command)
    
    conn.commit()
    print('SUCCESS: Seeded comprehensive test data.')
except Exception as e:
    print('ERROR:', str(e))
finally:
    if 'cursor' in locals(): cursor.close()
    if 'conn' in locals(): conn.close()
