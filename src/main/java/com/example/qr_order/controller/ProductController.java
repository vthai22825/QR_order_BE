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

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<Product>> createProduct(
                @Valid @RequestPart("data") ProductRequest request,
                @RequestPart(value = "file", required = false) MultipartFile file
        ) {
                Product newProduct = productService.create(request, file);

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

                ApiResponse<Product> response = ApiResponse.<Product>builder()
                        .status(HttpStatus.OK.value())
                        .message("Update product successful")
                        .data(updatedProduct)
                        .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                PageResponse<ProductResponse> result = productService.getAll(page, size);

                ApiResponse<PageResponse<ProductResponse>> response = ApiResponse
                                .<PageResponse<ProductResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Get all products successful")
                                .data(result)
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/category/{categoryId}")
        public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
                        @PathVariable Long categoryId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                PageResponse<ProductResponse> result = productService.getByCategoryId(categoryId, page, size);

                ApiResponse<PageResponse<ProductResponse>> response = ApiResponse
                                .<PageResponse<ProductResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Get products by category successful")
                                .data(result)
                                .build();

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
                productService.delete(id);

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
}
