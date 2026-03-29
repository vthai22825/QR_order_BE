package com.example.qr_order.dtos.response;

import com.example.qr_order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String tableName;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private List<OrderDetailResponse> items;
    private Instant createdAt;
}
