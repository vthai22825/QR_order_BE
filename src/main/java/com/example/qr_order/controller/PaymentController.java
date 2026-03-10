package com.example.qr_order.controller;

import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.OrderResponse;
import com.example.qr_order.enums.PaymentMethod;
import com.example.qr_order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @PostMapping("/table/{tableId}/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @PathVariable Long tableId,
                @RequestParam PaymentMethod paymentMethod // Nhận tham số qua URL (VD: ?paymentMethod=CASH)
    ) {
        OrderResponse paidOrder = orderService.checkout(tableId, paymentMethod);

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thanh toán thành công bằng " + paymentMethod)
                .data(paidOrder)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/order/{orderId}/generate-qr")
    public ResponseEntity<ApiResponse<String>> generateQRCode(@PathVariable Long orderId) {
        try {
            String checkoutUrl = orderService.generatePayOSLink(orderId);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Tạo mã QR thành công!")
                    .data(checkoutUrl)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder().message("Lỗi: " + e.getMessage()).build());
        }
    }
}
