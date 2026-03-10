package com.example.qr_order.service;

import com.example.qr_order.dtos.OrderDetailRequest;
import com.example.qr_order.dtos.OrderRequest;
import com.example.qr_order.dtos.response.OrderDetailResponse;
import com.example.qr_order.dtos.response.OrderResponse;
import com.example.qr_order.entity.DiningTable;
import com.example.qr_order.entity.Order;
import com.example.qr_order.entity.OrderDetail;
import com.example.qr_order.entity.Product;
import com.example.qr_order.enums.OrderStatus;
import com.example.qr_order.enums.PaymentMethod;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.repository.DiningTableRepo;
import com.example.qr_order.repository.OrderDetailRepo;
import com.example.qr_order.repository.OrderRepo;
import com.example.qr_order.repository.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrderDetailRepo orderDetailRepo;
    private final DiningTableRepo tableRepo;
    private final ProductRepo productRepo;
    private final PayOS payOS;


    @Transactional
    public OrderResponse processOrder(OrderRequest request) {

        // ==========================================
        // BƯỚC 1: KIỂM TRA BÀN ĂN
        // ==========================================
        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(request.getTableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        Order currentOrder;

        // ==========================================
        // BƯỚC 2: TÌM HOẶC TẠO MỚI HÓA ĐƠN (ORDER)
        // ==========================================
        if (table.getStatus() == TableStatus.AVAILABLE) {
            // Trường hợp 1: Bàn trống -> Khách mới vào -> Tạo Order mới
            currentOrder = new Order();
            currentOrder.setTable(table);
            currentOrder.setStatus(OrderStatus.UNPAID);
            currentOrder.setTotalPrice(BigDecimal.ZERO);
            currentOrder = orderRepo.save(currentOrder);

            // CẬP NHẬT TRẠNG THÁI BÀN SANG "CÓ KHÁCH"
            table.setStatus(TableStatus.OCCUPIED);
            tableRepo.save(table);
        } else {
            // Trường hợp 2: Bàn đã có khách -> Tìm cái Order đang UNPAID của bàn này
            currentOrder = orderRepo.findByTableIdAndStatusWithTable(table.getId(), OrderStatus.UNPAID)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Data error: The table has guests, but no unpaid bill was found."));
        }

        // ==========================================
        // BƯỚC 3: XỬ LÝ DANH SÁCH MÓN ĂN (ORDER DETAILS)
        // ==========================================
        BigDecimal totalAmountToAdd = BigDecimal.ZERO;

        for (OrderDetailRequest itemRequest : request.getItems()) {
            // 3.1: Kiểm tra món ăn có tồn tại và đang bán không?
            Product product = productRepo.findByIdActive(itemRequest.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The product with ID " + itemRequest.getProductId() + " does not exist or is no longer available for sale."));

            // 3.2: THUẬT TOÁN CỘNG DỒN (Tìm xem món này đã có trong Bill chưa)
            OrderDetail existingDetail = orderDetailRepo
                    .findByOrderIdAndProductId(currentOrder.getId(), product.getId())
                    .orElse(null);

            if (existingDetail != null) {
                // Đã gọi món này rồi -> Tăng số lượng lên
                existingDetail.setQuantity(existingDetail.getQuantity() + itemRequest.getQuantity());

                // Nối thêm ghi chú (Nếu khách note thêm)
                if (itemRequest.getNote() != null && !itemRequest.getNote().isEmpty()) {
                    String oldNote = existingDetail.getNote() == null ? "" : existingDetail.getNote() + " | ";
                    existingDetail.setNote(oldNote + itemRequest.getNote());
                }
                orderDetailRepo.save(existingDetail);

            } else {
                // Lần đầu gọi món này -> Tạo dòng mới
                OrderDetail newDetail = new OrderDetail();
                newDetail.setOrder(currentOrder);
                newDetail.setProduct(product);
                newDetail.setQuantity(itemRequest.getQuantity());

                // BÍ QUYẾT SENIOR: Lưu giá snapshot tại thời điểm đặt (Không dùng product.getPrice() về sau)
                newDetail.setPrice(product.getPrice());
                newDetail.setNote(itemRequest.getNote());
                orderDetailRepo.save(newDetail);
            }

            // 3.3: Cộng dồn tiền để lát nữa cập nhật tổng tiền Order
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmountToAdd = totalAmountToAdd.add(itemTotal);
        }

        // ==========================================
        // BƯỚC 4: CẬP NHẬT TỔNG TIỀN VÀ TRẢ VỀ
        // ==========================================
        currentOrder.setTotalPrice(currentOrder.getTotalPrice().add(totalAmountToAdd));
        Order savedOrder = orderRepo.save(currentOrder);

        // Map data trả về cho Frontend hiển thị Bill hiện tại
        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse checkout(Long tableId, PaymentMethod paymentMethod) {

        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        if (table.getStatus() == TableStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This table is empty; there is no bill to pay.");
        }

        Order currentOrder = orderRepo.findByTableIdAndStatusWithTable(table.getId(), OrderStatus.UNPAID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Data error: The table has guests, but no unpaid bill was found."));

        // Cập nhật phương thức thanh toán và trạng thái
        currentOrder.setPaymentMethod(paymentMethod); // <-- THÊM DÒNG NÀY
        currentOrder.setStatus(OrderStatus.PAID);
        orderRepo.save(currentOrder);

        table.setStatus(TableStatus.AVAILABLE);
        tableRepo.save(table);

        return mapToOrderResponse(currentOrder);
    }

    public String generatePayOSLink(Long orderId) throws Exception {
        // 1. Lấy thông tin đơn hàng từ Database của em
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Chuẩn bị thông tin thanh toán (Theo chuẩn v2)
        String description = "Thanh toan don hang " + order.getId();
        String returnUrl = "http://localhost:8386/success"; // Trang báo thành công
        String cancelUrl = "http://localhost:8386/cancel";   // Trang báo hủy
        long price = order.getTotalPrice().longValue();

        // 3. Tạo danh sách sản phẩm (Items) để hiển thị trên PayOS
        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Hóa đơn bàn " + order.getTable().getId())
                .quantity(1)
                .price((Long) price)
                .build();

        // 4. Xây dựng Request tạo link thanh toán
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(order.getId()) // Sử dụng ID đơn hàng của em làm mã quản lý
                .amount(price)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item) // Thêm item vào để hiển thị chi tiết
                .build();

        // 5. Gọi API PayOS để tạo link
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

        // Trả về cái link thanh toán cho Controller
        return response.getCheckoutUrl();
    }

    @Transactional // Rất quan trọng: Đảm bảo dữ liệu không bị lỗi nửa chừng
    public void checkoutByOrderId(Long orderId, PaymentMethod paymentMethod) throws Exception {

        // 1. Tìm hóa đơn trong Database
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn với ID: " + orderId));

        // 2. Chặn đánh kép: Kiểm tra nếu hóa đơn ĐÃ THANH TOÁN rồi thì bỏ qua
        // (Phòng trường hợp mạng lag, PayOS bắn Webhook 2 lần cho cùng 1 đơn)
        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println("⚠️ Hóa đơn " + orderId + " đã được thanh toán từ trước!");
            return;
        }

        // 3. Cập nhật trạng thái Hóa đơn
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(paymentMethod); // Sẽ nhận BANK_TRANSFER từ Webhook

        // 4. (Tùy chọn) Xử lý dọn bàn
        // Nếu nghiệp vụ của em là khách trả tiền xong -> Bàn thành ghế trống
        if (order.getTable() != null) {
            order.getTable().setStatus(TableStatus.AVAILABLE);
            // Nếu có tableRepo thì bật dòng dưới lên:
            // tableRepo.save(order.getTable());
        }

        // 5. Lưu cập nhật xuống Database
        orderRepo.save(order);

        System.out.println("✅ ĐÃ GẠCH NỢ VÀ DỌN BÀN THÀNH CÔNG CHO ĐƠN: " + orderId);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        // Lấy lại danh sách chi tiết (Dùng hàm join fetch chống N+1)
        List<OrderDetail> details = orderDetailRepo.findByOrderIdWithProduct(order.getId());

        List<OrderDetailResponse> detailResponses = details.stream().map(d -> {
            BigDecimal amount = d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            return new OrderDetailResponse(
                    d.getId(),
                    d.getProduct().getName(),
                    d.getQuantity(),
                    d.getPrice(),
                    amount,
                    d.getNote()
            );
        }).toList();

        return new OrderResponse(
                order.getId(),
                order.getTable().getName(),
                order.getStatus(),
                order.getTotalPrice(),
                detailResponses
        );
    }

    public OrderResponse getActiveOrderByTable(Long tableId) {
        // Gọi xuống DB tìm đơn hàng UNPAID của bàn này
        Order activeOrder = orderRepo.findActiveOrderByTableId(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bàn này hiện chưa có đơn hàng nào đang hoạt động"));

        // Map sang DTO để trả về cho an toàn (Nhớ tạo hàm mapToOrderResponse nếu em chưa có nhé)
        return mapToOrderResponse(activeOrder);
    }
}
