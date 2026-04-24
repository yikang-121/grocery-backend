package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.AccountingSummaryDTO;
import com.grocery.grocerybackend.dto.PurchaseOrderDTO;
import com.grocery.grocerybackend.dto.StockMovementDTO;
import com.grocery.grocerybackend.service.AccountingService;
import com.grocery.grocerybackend.service.PurchaseOrderService;
import com.grocery.grocerybackend.service.StockMovementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/accounting")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountingController {

    private final AccountingService accountingService;
    private final StockMovementService stockMovementService;
    private final PurchaseOrderService purchaseOrderService;

    public AccountingController(AccountingService accountingService,
                                StockMovementService stockMovementService,
                                PurchaseOrderService purchaseOrderService) {
        this.accountingService = accountingService;
        this.stockMovementService = stockMovementService;
        this.purchaseOrderService = purchaseOrderService;
    }

    // ===== Accounting Summary =====

    @GetMapping("/summary")
    public AccountingSummaryDTO getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        return accountingService.getAccountingSummary(y, m);
    }

    @GetMapping("/invoice/{orderNo}")
    public Map<String, Object> getInvoice(@PathVariable String orderNo) {
        return accountingService.getInvoiceForOrder(orderNo);
    }

    // ===== Stock Movements =====

    @GetMapping("/stock-movements")
    public List<StockMovementDTO> getStockMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return stockMovementService.getMovements(productId, type, from, to);
    }

    @GetMapping("/stock-movements/summary")
    public Map<String, Object> getStockMovementSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return stockMovementService.getStockInOutSummary(from, to);
    }

    // ===== Purchase Orders =====

    @GetMapping("/purchase-orders")
    public List<PurchaseOrderDTO> listPurchaseOrders(
            @RequestParam(required = false) String status) {
        return purchaseOrderService.listPOs(status);
    }

    @GetMapping("/purchase-orders/{id}")
    public PurchaseOrderDTO getPurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.getPO(id);
    }

    @PostMapping("/purchase-orders/auto-generate")
    public ResponseEntity<?> autoGeneratePO() {
        PurchaseOrderDTO po = purchaseOrderService.autoGeneratePO();
        if (po == null) {
            return ResponseEntity.ok(Map.of("message", "No items need restocking at this time."));
        }
        return ResponseEntity.ok(po);
    }

    @PostMapping("/purchase-orders/{id}/approve")
    public PurchaseOrderDTO approvePO(@PathVariable Long id) {
        return purchaseOrderService.approvePO(id);
    }

    @PostMapping("/purchase-orders/{id}/receive")
    public PurchaseOrderDTO receivePO(@PathVariable Long id) {
        return purchaseOrderService.receivePO(id);
    }

    @GetMapping("/purchase-orders/{id}/export-csv")
    public ResponseEntity<byte[]> exportPOCSV(@PathVariable Long id) {
        String csv = purchaseOrderService.exportPOtoCSV(id);
        String filename = "PO_Export_" + id + ".csv";
        
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
