// src/main/java/com/grocery/grocerybackend/dto/AddressRequest.java
package com.grocery.grocerybackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddressRequest {
    @NotNull
    public Long userId;

    public String label;              // optional
    @NotBlank
    public String name;

    public String phone;              // optional

    // Accept "addressLine" OR "address_line"
    @NotBlank
    @JsonAlias({"address_line"})
    public String addressLine;

    @NotBlank
    public String city;

    public String state;              // optional

    // Accept "postal" OR "postalCode"
    @NotBlank
    @JsonAlias({"postalCode"})
    public String postal;

    // Accept "isDefault" OR "is_default"
    @JsonAlias({"is_default"})
    public Boolean isDefault;         // optional; default false
}
