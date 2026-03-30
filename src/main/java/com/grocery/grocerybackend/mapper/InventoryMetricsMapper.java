package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryMetricsMapper extends BaseMapper<InventoryMetrics> {

    @Select("""
            SELECT 
                p.sku as skuId,
                p.stock_quantity as currentStock,
                COALESCE(SUM(CASE WHEN o.created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY) THEN oi.quantity ELSE 0 END) / 3.0, 0) as avgSales3d,
                COALESCE(SUM(CASE WHEN o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN oi.quantity ELSE 0 END) / 30.0, 0) as avgSales30d,
                (SELECT STDDEV_POP(daily_qty) FROM (
                    SELECT SUM(oi2.quantity) as daily_qty 
                    FROM order_items oi2 
                    JOIN orders o2 ON oi2.order_id = o2.id 
                    WHERE oi2.product_id = p.id AND o2.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) 
                    GROUP BY DATE(o2.created_at)
                ) as stats) as stdDev30d,
                COALESCE(
                    (SELECT SUM(oi3.quantity) / 30.0 
                     FROM order_items oi3 
                     JOIN orders o3 ON oi3.order_id = o3.id 
                     WHERE oi3.product_id = p.id 
                     AND MONTH(o3.created_at) = MONTH(NOW()) 
                     AND YEAR(o3.created_at) = YEAR(NOW()) - 1
                    ) / 
                    (SELECT SUM(oi4.quantity) / GREATEST(DATEDIFF(NOW(), MIN(o4.created_at)), 1)
                     FROM order_items oi4 
                     JOIN orders o4 ON oi4.order_id = o4.id 
                     WHERE oi4.product_id = p.id
                    ), 1.0) as seasonalityFactor
            FROM product p
            LEFT JOIN order_items oi ON p.id = oi.product_id
            LEFT JOIN orders o ON oi.order_id = o.id
            GROUP BY p.id, p.sku, p.stock_quantity
            """)
    List<InventoryMetrics> calculateSalesStats();

    @Select("SELECT * FROM inventory_metrics WHERE sku_id = #{skuId} LIMIT 1")
    InventoryMetrics findBySkuId(@Param("skuId") String skuId);
}
