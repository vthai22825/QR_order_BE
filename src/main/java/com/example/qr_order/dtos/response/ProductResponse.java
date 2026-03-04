package com.example.qr_order.dtos.response;


import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;

    private Boolean isBestSeller;

    private BigDecimal salePrice;
    private boolean isPromoted;
    private String promotionTag;
}
