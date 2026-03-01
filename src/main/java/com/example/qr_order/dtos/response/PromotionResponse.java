package com.example.qr_order.dtos.response;


import com.example.qr_order.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String name;
    private String description;
    private String promotionImage;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Instant startDate;
    private Instant endDate;
    private boolean isActive;
    private boolean isValidNow;
}
