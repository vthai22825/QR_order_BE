package com.example.qr_order.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CategoryRequest {

    @NotBlank(message = "Category's name is required")
    private String categoryName;

    private String categoryDescription;
}
