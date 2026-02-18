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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;

        @PostMapping
        public ResponseEntity<ApiResponse<Product>> createProduct(
                        @ModelAttribute @Valid ProductRequest productRequest) {
                Product newProduct = productService.create(productRequest);

                ApiResponse<Product> apiResponse = ApiResponse.<Product>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Create product successful")
                                .data(newProduct)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<Product>> updateProduct(
                        @PathVariable Long id,
                        @ModelAttribute @Valid ProductRequest request) {
                Product updatedProduct = productService.update(id, request);

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

}
