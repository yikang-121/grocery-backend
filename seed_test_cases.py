import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="1234",
        database="grocery_db"
    )
    cursor = conn.cursor()

    products = [
        ("TC-018: High Momentum Prod", "Produce", 10.00, 10, "TC018"),
        ("TC-019: Short Shelf Life Prod", "Dairy", 5.00, 0, "TC019"),
        ("TC-020: Zero Sales Prod", "Snack", 2.00, 5, "TC020"),
        ("TC-021: High Spoilage Prod", "Produce", 8.00, 0, "TC021"),
        ("TC-022: Restock Trigger Prod", "Canned", 3.00, 2, "TC022"),
    ]

    for p in products:
        cursor.execute("DELETE FROM product WHERE sku = %s", (p[4],))
        cursor.execute("DELETE FROM inventory_metrics WHERE sku_id = %s", (p[4],))
        
        cursor.execute(
            "INSERT INTO product (name, category, price, stock_quantity, sku) VALUES (%s, %s, %s, %s, %s)",
            p
        )

    metrics = [
        # sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor, incoming_stock, case_size
        ("TC018", 10, 2, 1, 30, 1, 0.0, 50.0, 20.0, 5.0, 1.0, 0, 1),
        ("TC019", 0, 5, 2, 3, 1, 0.0, 20.0, 20.0, 2.0, 1.0, 0, 1),
        ("TC020", 5, 2, 1, 30, 10, 0.0, 0.0, 0.0, 0.0, 1.0, 0, 1),
        ("TC021", 0, 2, 2, 5, 1, 0.2, 30.0, 30.0, 5.0, 1.0, 0, 1),
        ("TC022", 2, 3, 1, 90, 50, 0.001, 15.0, 15.0, 3.0, 1.0, 0, 1),
    ]

    for m in metrics:
        cursor.execute(
            """INSERT INTO inventory_metrics 
            (sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor, incoming_stock, case_size) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
            m
        )
        
    conn.commit()
    print("Test cases TC-018 to TC-022 seeded successfully!")
    
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
