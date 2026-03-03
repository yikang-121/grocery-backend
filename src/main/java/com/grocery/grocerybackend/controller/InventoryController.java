// src/main/java/com/grocery/grocerybackend/controller/InventoryController.java
package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.Batch;
import com.grocery.grocerybackend.service.InventoryService;
import com.grocery.grocerybackend.service.InventoryService.BulkUploadResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/restock")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BulkUploadResult bulkUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "profitMargin", required = false) BigDecimal profitMargin) throws Exception {
        return inventoryService.uploadCsv(file, profitMargin);
    }

    @PostMapping("/batch")
    public Batch addBatch(@RequestBody Batch batch) {
        return inventoryService.addBatch(batch);
    }

    @PostMapping("/spoilage")
    public void recordSpoilage(@RequestBody com.grocery.grocerybackend.dto.SpoilageRequest req) {
        inventoryService.recordSpoilage(req.getBatchId(), req.getQuantity(), req.getReason());
    }

    @PostMapping("/cleanup-expired")
    public int cleanupExpired() {
        return inventoryService.cleanupExpiredBatches();
    }

    @GetMapping("/spoilage")
    public java.util.List<com.grocery.grocerybackend.dto.SpoilageLogResponse> getSpoilageLogs() {
        return inventoryService.getSpoilageLogs();
    }
}
