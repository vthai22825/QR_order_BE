package com.example.qr_order.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDetailRequest {
    @NotNull(message = "product's id cannot be null")
    private Long productId;

    @Min(value = 1, message = "Quantity at least one")
    private int quantity;

    private String note;
}
