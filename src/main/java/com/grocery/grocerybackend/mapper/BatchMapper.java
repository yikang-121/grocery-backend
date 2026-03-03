package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BatchMapper extends BaseMapper<Batch> {

    /**
     * Finds valid batches (not expired, qty > 0) for FEFO deduction.
     * Uses row-level locking (FOR UPDATE) to prevent race conditions.
     */
    @Select("SELECT * FROM batch WHERE product_id = #{productId} " +
            "AND available_quantity > 0 " +
            "AND expiry_date >= #{today} " +
            "ORDER BY expiry_date ASC " +
            "FOR UPDATE")
    List<Batch> findValidBatchesForFefo(@Param("productId") Long productId, @Param("today") LocalDate today);
}
