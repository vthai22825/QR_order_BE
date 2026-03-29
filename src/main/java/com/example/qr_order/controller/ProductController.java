package com.example.qr_order.controller;

import com.example.qr_order.dtos.ProductRequest;
import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.PageResponse;
import com.example.qr_order.dtos.response.ProductResponse;
import com.example.qr_order.entity.Product;
import com.example.qr_order.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;
        private final SimpMessagingTemplate messagingTemplate;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<Product>> createProduct(
                @Valid @RequestPart("data") ProductRequest request,
                @RequestPart(value = "file", required = false) MultipartFile file
        ) {
                Product newProduct = productService.create(request, file);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Thêm mới)");

                ApiResponse<Product> apiResponse = ApiResponse.<Product>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Create product successful")
                        .data(newProduct)
                        .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }

        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<Product>> updateProduct(
                @PathVariable Long id,
                @Valid @RequestPart("data") ProductRequest request,
                @RequestPart(value = "file", required = false) MultipartFile file
        ) {
                Product updatedProduct = productService.update(id, request, file);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Sửa đổi)");

                ApiResponse<Product> response = ApiResponse.<Product>builder()
                        .status(HttpStatus.OK.value())
                        .message("Update product successful")
                        .data(updatedProduct)
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

                List<ProductResponse> products = productService.getAllProducts();

                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy toàn bộ thực đơn thành công")
                        .data(products)
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/category/{categoryId}")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategoryId(@PathVariable Long categoryId) {

                List<ProductResponse> products = productService.getByCategoryId(categoryId);

                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách món ăn theo danh mục thành công")
                        .data(products) // Nhét thẳng cái List vào
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/promotion/{promotionId}")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getByPromotionId(@PathVariable Long promotionId) {

                List<ProductResponse> products = productService.getByPromotionId(promotionId);

                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách món ăn theo khuyến mãi thành công")
                        .data(products)
                        .build();

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
                productService.delete(id);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Xoá)");

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("Delete product successful")
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/apply-promotion/{promotionId}")
        public ResponseEntity<ApiResponse<Void>> applyPromotion(
                @PathVariable Long promotionId,
                @RequestBody List<Long> productIds
        ) {
                productService.applyPromotionToProducts(promotionId, productIds);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Áp dụng KM)");

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Áp dụng khuyến mãi cho sản phẩm thành công!")
                        .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/remove-promotion")
        public ResponseEntity<ApiResponse<Void>> removePromotion(
                @RequestBody List<Long> productIds
        ) {
                productService.removePromotionFromProducts(productIds);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Gỡ KM)");

                ApiResponse<Void> response = ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Đã gỡ khuyến mãi khỏi các sản phẩm được chọn!")
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProduct(@RequestParam("keyword") String keyword){
                List<ProductResponse> searchResult = productService.searchProduct(keyword);

                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Tìm kiếm thành công")
                        .data(searchResult)
                        .build();
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}/best-seller")
        public ResponseEntity<ApiResponse<ProductResponse>> updateBestSellerStatus(
                @PathVariable Long id,
                @RequestParam("status") boolean status // Nhận true hoặc false từ URL
        ) {
                ProductResponse updatedProduct = productService.updateBestSellerStatus(id, status);
                messagingTemplate.convertAndSend("/topic/products", "Danh sách sản phẩm mới được cập nhật (Trending)");

                ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message(status ? "Đã thêm món vào danh sách Best Seller!" : "Đã gỡ món khỏi danh sách Best Seller!")
                        .data(updatedProduct)
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/best-sellers")
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellers() {
                List<ProductResponse> bestSellers = productService.getBestSellers();

                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách món bán chạy thành công")
                        .data(bestSellers)
                        .build();

                return ResponseEntity.ok(response);
        }
}
