// src/main/java/com/grocery/grocerybackend/mapper/ProductMapper.java
package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product WHERE sku = #{sku} LIMIT 1")
    Product findBySku(@Param("sku") String sku);

    @Update("UPDATE product SET stock_quantity = stock_quantity - #{qty} " +
            "WHERE id = #{productId} AND stock_quantity >= #{qty}")
    int decrementStock(@Param("productId") Long productId, @Param("qty") Integer qty);

    @Update("UPDATE product SET stock_quantity = stock_quantity + #{qty} WHERE id = #{productId}")
    int incrementStock(@Param("productId") Long productId, @Param("qty") Integer qty);

    @Select("""
        SELECT id, sku, name, category, price, original_price, image_url,
               rating, rating_count, stock_quantity
        FROM product
        ORDER BY COALESCE(rating,0) DESC,
                 COALESCE(rating_count,0) DESC
        LIMIT #{limit}
    """)
    List<Product> selectTopRated(@Param("limit") int limit);

}
