package com.grocery.grocerybackend;

import com.grocery.grocerybackend.entity.Batch;
import com.grocery.grocerybackend.exception.InsufficientStockException;
import com.grocery.grocerybackend.mapper.BatchMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import com.grocery.grocerybackend.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryServiceFEFOTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private BatchMapper batchMapper;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeductStockFEFO_SingleBatch_Success() {
        Long productId = 1L;
        int quantity = 5;
        LocalDate today = LocalDate.now();

        Batch batch = new Batch();
        batch.setId(101L);
        batch.setAvailableQuantity(10);
        batch.setExpiryDate(today.plusDays(10));

        when(batchMapper.findValidBatchesForFefo(eq(productId), any())).thenReturn(Collections.singletonList(batch));

        inventoryService.deductStockFEFO(productId, quantity);

        assertEquals(5, batch.getAvailableQuantity());
        verify(batchMapper, times(1)).updateById(batch);
        verify(productMapper, times(1)).decrementStock(productId, quantity);
    }

    @Test
    void testDeductStockFEFO_MultipleBatches_Success() {
        Long productId = 1L;
        int quantity = 15;
        LocalDate today = LocalDate.now();

        Batch batch1 = new Batch();
        batch1.setId(101L);
        batch1.setAvailableQuantity(10);
        batch1.setExpiryDate(today.plusDays(5));

        Batch batch2 = new Batch();
        batch2.setId(102L);
        batch2.setAvailableQuantity(10);
        batch2.setExpiryDate(today.plusDays(10));

        List<Batch> batches = Arrays.asList(batch1, batch2);
        when(batchMapper.findValidBatchesForFefo(eq(productId), any())).thenReturn(batches);

        inventoryService.deductStockFEFO(productId, quantity);

        assertEquals(0, batch1.getAvailableQuantity());
        assertEquals(5, batch2.getAvailableQuantity());
        verify(batchMapper, times(2)).updateById(any(Batch.class));
        verify(productMapper, times(1)).decrementStock(productId, quantity);
    }

    @Test
    void testDeductStockFEFO_InsufficientStock_ThrowsException() {
        Long productId = 1L;
        int quantity = 25;

        Batch batch1 = new Batch();
        batch1.setAvailableQuantity(10);

        when(batchMapper.findValidBatchesForFefo(eq(productId), any())).thenReturn(Collections.singletonList(batch1));

        assertThrows(InsufficientStockException.class, () -> {
            inventoryService.deductStockFEFO(productId, quantity);
        });

        verify(batchMapper, never()).updateById(any());
        verify(productMapper, never()).decrementStock(anyLong(), anyInt());
    }

    @Test
    void testDeductStockFEFO_ZeroQuantity_DoesNothing() {
        inventoryService.deductStockFEFO(1L, 0);
        verify(batchMapper, never()).findValidBatchesForFefo(anyLong(), any());
    }
}
