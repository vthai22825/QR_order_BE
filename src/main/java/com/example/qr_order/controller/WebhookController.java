package com.example.qr_order.controller;

import com.example.qr_order.enums.PaymentMethod;
import com.example.qr_order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final OrderService orderService;
    private final PayOS payOS;

    @PostMapping("/payos")
    public ResponseEntity<?> handlePayosWebhook(@RequestBody Object body) {
        try {
            // 1. Sử dụng hàm verify của PayOS để kiểm tra chữ ký (Checksum)
            WebhookData data = payOS.webhooks().verify(body);

            // 2. Lấy mã đơn hàng (orderCode) từ dữ liệu trả về
            Long orderId = data.getOrderCode();

            // 3. Dùng try-catch để bắt lỗi từ Service ném ra
            try {
                // Gọi Service gạch nợ. Nếu không thấy đơn (ví dụ ID 123), Service sẽ ném Exception
                orderService.checkoutByOrderId(orderId, PaymentMethod.BANK_TRANSFER);
                log.info("🔔 Nhận thông báo thanh toán thành công cho đơn hàng thật: {}", orderId);

            } catch (Exception e) {
                // Bắt được lỗi "Không tìm thấy hóa đơn..." -> In ra cảnh báo màu vàng
                log.warn("⚠️ Bỏ qua xử lý Database: {}", e.getMessage());
            }

            // 4. LUÔN LUÔN trả về 200 OK để PayOS biết ta đã nhận được, không gửi lại nữa
            return ResponseEntity.ok(Map.of("message", "Đã nhận Webhook", "orderId", orderId));

        } catch (Exception e) {
            // Lỗi này văng ra khi chữ ký (Signature) bị sai, dữ liệu bị giả mạo
            log.error("❌ Lỗi xác thực Webhook (Sai chữ ký): {}", e.getMessage());
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }
    }
}
