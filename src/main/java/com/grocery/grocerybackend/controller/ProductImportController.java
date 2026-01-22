/*package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService importService;

    @PostMapping("/import")
    public Map<String, Object> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dryRun", required = false, defaultValue = "false") boolean dryRun
    ) throws Exception {
        return importService.importCsv(file, dryRun);
    }
}
*/