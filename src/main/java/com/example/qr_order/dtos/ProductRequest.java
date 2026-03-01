package com.example.qr_order.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name cannot be null")
    @Size(max = 200, message = "Product name not exceeding 200 character")
    private String name;

    @Size(max = 400, message = "Product description not exceeding 400 character")
    private String description;


    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "the price must be greater than zero")
    private BigDecimal price;

    private Long categoryId;
}
