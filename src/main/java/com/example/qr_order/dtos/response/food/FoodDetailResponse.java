package com.example.qr_order.dtos.response.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private CategorySummary category;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySummary {
        private Long id;
        private String name;
    }
}
