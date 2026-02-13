package com.example.qr_order.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name not null")
    @Size(max = 30, message = "Category name not exceeding 30 character")
    private String name;

    @Size(max = 400, message = "Category description not exceeding 400 character")
    private String description;
}
