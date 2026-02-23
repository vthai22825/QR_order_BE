package com.example.qr_order.controller;

import com.example.qr_order.dtos.OrderRequest;
import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.OrderResponse;
import com.example.qr_order.enums.PaymentMethod;
import com.example.qr_order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse currentOrder = orderService.processOrder(request);

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đặt món thành công!")
                .data(currentOrder)
                .build();

        return ResponseEntity.ok(response);
    }

}
