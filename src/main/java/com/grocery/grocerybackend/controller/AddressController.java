// src/main/java/com/grocery/grocerybackend/controller/UserAddressController.java
package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.AddressRequest;
import com.grocery.grocerybackend.entity.UserAddress;
import com.grocery.grocerybackend.service.UserAddressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins =
        {       "http://localhost:3000",
                "http://localhost:8081"
})

public class AddressController {    

    private final UserAddressService service;

    public AddressController(UserAddressService service) {
        this.service = service;
    }

    // GET /api/addresses?userId=123
    @GetMapping
    public List<UserAddress> list(@RequestParam Long userId) {
        return service.listByUser(userId);
    }

    // POST /api/addresses
    @PostMapping
    public UserAddress create(@Valid @RequestBody AddressRequest req) {
        return service.create(req);
    }

    // PUT /api/addresses/{id}
    @PutMapping("/{id}")
    public UserAddress update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return service.update(id, req);
    }

    // PATCH /api/addresses/{id}/default?userId=123
    @PatchMapping("/{id}/default")
    public void setDefault(@PathVariable Long id, @RequestParam Long userId) {
        service.setDefault(userId, id);
    }

    // DELETE /api/addresses/{id}?userId=123
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(userId, id);
    }
}
