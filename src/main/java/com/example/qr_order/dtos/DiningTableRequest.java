package com.example.qr_order.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiningTableRequest {
    @NotBlank(message = "Table's name cannot be null")
    private String name;
}