package com.grocery.grocerybackend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbSeeder {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/grocery_db?useSSL=false&allowPublicKeyRetrieval=true",
                "root", "1234"
            );
            Statement stmt = conn.createStatement();
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            
            String[] queries = {
                "DELETE FROM inventory_metrics WHERE sku_id IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005')",
                "DELETE FROM spoilage_log WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005'))",
                "DELETE FROM order_items WHERE product_id IN (SELECT id FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005'))",
                "DELETE FROM product WHERE sku IN ('VG-OAT-001', 'VG-STW-002', 'VG-WAG-003', 'VG-TRK-004', 'VG-MNC-005')",
                "INSERT INTO product (sku, name, category, price, stock_quantity) VALUES " +
                "('VG-OAT-001', 'Organic Oats (Momentum)', 'Dairy', 5.50, 10), " +
                "('VG-STW-002', 'Fresh Strawberries (Perishability)', 'Produce', 6.99, 0), " +
                "('VG-WAG-003', 'Wagyu Beef (Zero Sales)', 'Meat', 85.00, 5), " +
                "('VG-TRK-004', 'Turkey Mince (Decay)', 'Meat', 45.00, 0), " +
                "('VG-MNC-005', 'Mooncakes (Seasonality)', 'Bakery', 12.00, 10)",
                "INSERT INTO inventory_metrics " +
                "(sku_id, current_stock, lead_time_days, review_period_days, shelf_life_days, supplier_moq, case_size, waste_lambda, avg_sales_3d, avg_sales_30d, std_dev_30d, seasonality_factor, incoming_stock) " +
                "VALUES " +
                "('VG-OAT-001', 10, 3, 7, 90, 1, 1, 0.01, 50.0, 20.0, 5.0, 1.0, 0), " +
                "('VG-STW-002', 0,  5, 2,  3, 1, 1, 0.00, 20.0, 20.0, 2.0, 1.0, 0), " +
                "('VG-WAG-003', 5,  2, 1, 30,10, 6, 0.05,  0.0,  0.0, 0.0, 1.0, 0), " +
                "('VG-TRK-004', 0,  2, 2,  5, 1, 1, 0.20, 30.0, 30.0, 5.0, 1.0, 0), " +
                "('VG-MNC-005', 10, 3, 7, 30, 1, 1, 0.01, 25.0, 25.0, 4.0, 1.5, 0)"
            };
            
            for (String q : queries) {
                stmt.execute(q);
                System.out.println("Executed successfully");
            }
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
            
            stmt.close();
            conn.close();
            System.out.println("DATABASE SEEDED SUCCESSFULLY!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
