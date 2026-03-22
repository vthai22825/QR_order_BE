package com.example.qr_order.dtos.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse {
    private String period;
    private BigDecimal revenue;
    private long orderCount;
}
