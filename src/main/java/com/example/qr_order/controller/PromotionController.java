package com.example.qr_order.controller;

import com.example.qr_order.dtos.PromotionRequest;
import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.entity.Promotion;
import com.example.qr_order.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(
            @Valid @RequestPart("data") PromotionRequest request, // Hứng JSON ngon lành cành đào
            @RequestPart(value = "file", required = false) MultipartFile file // Hứng file riêng biệt
    ) {
        // Truyền thẳng request và file xuống Service
        Promotion savedPromotion = promotionService.create(request, file);

        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo khuyến mãi mới thành công!")
                .data(savedPromotion)
                .build();

        return ResponseEntity.ok(response);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(
            @PathVariable Long id,
            @RequestPart("data") PromotionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        Promotion updatedPromotion = promotionService.update(id, request, file);

        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật khuyến mãi thành công!")
                .data(updatedPromotion)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Promotion>> getPromotionById(@PathVariable Long id) {
        Promotion promotion = promotionService.getPromotionById(id);

        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin khuyến mãi thành công!")
                .data(promotion)
                .build();

        return ResponseEntity.ok(response);
    }

    // 4. LẤY DANH SÁCH KHUYẾN MÃI (Hỗ trợ Search)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Promotion>>> getAllPromotions(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Promotion> promotions = promotionService.getAllPromotions(name, page, size);

        ApiResponse<Page<Promotion>> response = ApiResponse.<Page<Promotion>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách khuyến mãi thành công!")
                .data(promotions)
                .build();

        return ResponseEntity.ok(response);
    }

    // 5. XÓA MỀM (Không cần trả về data)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã xóa khuyến mãi thành công!")
                .build();

        return ResponseEntity.ok(response);
    }

    // 6. BẬT/TẮT NHANH TRẠNG THÁI
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleActiveStatus(@PathVariable Long id) {
        promotionService.toggleActiveStatus(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã thay đổi trạng thái hoạt động của khuyến mãi!")
                .build();

        return ResponseEntity.ok(response);
    }
}
