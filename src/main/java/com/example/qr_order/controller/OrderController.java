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

import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

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

        // Gửi thông báo WebSocket tới Thu ngân khi có đơn hàng mới
        messagingTemplate.convertAndSend("/topic/orders", "Đơn hàng mới được tạo hoặc cập nhật");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<ApiResponse<OrderResponse>> getActiveOrder(@PathVariable Long tableId) {

        OrderResponse response = orderService.getActiveOrderByTable(tableId);

        ApiResponse<OrderResponse> apiResponse = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin đơn hàng hiện tại thành công")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
