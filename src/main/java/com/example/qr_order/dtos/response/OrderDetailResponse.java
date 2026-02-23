package com.example.qr_order.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String note;
}
