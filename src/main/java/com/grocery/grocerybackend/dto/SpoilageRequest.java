package com.grocery.grocerybackend.dto;

import lombok.Data;

@Data
public class SpoilageRequest {
    private Long batchId;
    private Integer quantity;
    private String reason;
}
