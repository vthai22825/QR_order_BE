package com.example.qr_order.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodRequest {
    private String foodName;
    private String foodDescription;
    private Double foodPrice;
    private String foodImage;
    private Long categoryId;
}
