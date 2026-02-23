package com.example.qr_order.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "Table's id cannot be null")
    private Long tableId;

    @NotEmpty(message = "Cart cannot be null")
    @Valid
    private List<OrderDetailRequest> items;
}
