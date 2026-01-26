package com.example.qr_order.dtos.response.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodResponse {
    private Long foodId;
    private String foodName;
    private String foodDescription;
    private Double foodPrice;
    private String foodImage;
    private Long categoryId;
}
