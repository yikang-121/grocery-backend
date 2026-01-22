// src/main/java/com/grocery/grocerybackend/dto/AddressResponse.java
package com.grocery.grocerybackend.dto;

import java.time.LocalDateTime;

public class AddressResponse {
    public Long id, userId;
    public String label, name, phone, addressLine, city, state, postal;
    public Boolean isDefault;
    public LocalDateTime createdAt, updatedAt;
}
