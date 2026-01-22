// dto/CancelOrderRequest.java
package com.grocery.grocerybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelOrderRequest {
    @NotBlank public String reason;              // e.g. "Ordered by mistake"
    @NotNull  public Action action;              // REFUND or SUBSTITUTE
    public String extraNotes;                    // optional

    public enum Action { REFUND, SUBSTITUTE }
}
