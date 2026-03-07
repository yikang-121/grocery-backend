package com.grocery.grocerybackend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentIntentRequest {
    private BigDecimal amount;
    private String currency; // e.g., "myr"
}
