package com.example.qr_order.service;

import com.example.qr_order.dtos.ProductRequest;
import com.example.qr_order.dtos.response.PageResponse;
import com.example.qr_order.dtos.response.ProductResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.entity.Product;
import com.example.qr_order.entity.Promotion;
import com.example.qr_order.enums.DiscountType;
import com.example.qr_order.repository.CategoryRepo;
import com.example.qr_order.repository.ProductRepo;
import com.example.qr_order.repository.PromotionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ImageStorageService imageStorageService;
    private final PromotionRepo promotionRepo;

    @Transactional
    public Product create(ProductRequest productRequest) {
        Category category = categoryRepo.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "category with id " + productRequest.getCategoryId() + " not found"));

        if (category.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot created product with deleted category");
        }

        if (productRepo.existsByNameAndIsDeletedFalse(productRequest.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product name '" + productRequest.getName() + "' already exists");
        }

        Product newProduct = new Product();
        newProduct.setName(productRequest.getName());
        newProduct.setDescription(productRequest.getDescription());

        newProduct.setPrice(productRequest.getPrice());
        newProduct.setCategory(category);

        Product savedProduct = productRepo.save(newProduct);

        if (productRequest.getImage() != null && !productRequest.getImage().isEmpty()) {
            imageStorageService.uploadImage(productRequest.getImage(), "products", (imageUrl) -> {
                savedProduct.setImageUrl(imageUrl);
                productRepo.save(savedProduct);
            });
        }

        return savedProduct;
    }

    @Transactional
    public Product update(Long id, ProductRequest productRequest) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (productRequest.getCategoryId() != null
                && !productRequest.getCategoryId().equals(existing.getCategory().getId())) {

            Category newCategory = categoryRepo.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

            if (newCategory.isDeleted()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move to a deleted category");
            }

            existing.setCategory(newCategory);
        }

        if (productRequest.getName() != null && !productRequest.getName().equals(existing.getName())) {

            if (productRepo.existsByNameAndIsDeletedFalse(productRequest.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
            }

            existing.setName(productRequest.getName());
        }

        if (productRequest.getDescription() != null) {
            existing.setDescription(productRequest.getDescription());
        }
        if (productRequest.getImage() != null && !productRequest.getImage().isEmpty()) {
            imageStorageService.uploadImage(productRequest.getImage(), "products", (imageUrl) -> {
                existing.setImageUrl(imageUrl);
                productRepo.save(existing);
            });
        }
        if (productRequest.getPrice() != null) {
            existing.setPrice(productRequest.getPrice());
        }

        return productRepo.save(existing);

    }

    public PageResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepo.findAllActive(pageable);

        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .total(productPage.getTotalElements())
                .items(productResponses)
                .build();
    }

    public PageResponse<ProductResponse> getByCategoryId(Long categoryId, int page, int size) {

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if(category.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category has been deleted");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepo.findByCategoryId(categoryId, pageable);

        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .total(productPage.getTotalElements())
                .items(productResponses)
                .build();
    }
    @Transactional
    public void delete(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));

        product.setDeleted(true);
        productRepo.save(product);
    }

    @Transactional
    public void applyPromotionToProducts(Long promotionId, List<Long> productIds) {
        // Kiểm tra KM có tồn tại không
        Promotion promotion = promotionRepo.findByIdAndIsDeletedFalse(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));

        // Lấy danh sách món ăn và gắn KM vào
        List<Product> products = productRepo.findAllById(productIds);
        for (Product product : products) {
            product.setPromotion(promotion);
        }
        productRepo.saveAll(products);
    }

    @Transactional
    public void removePromotionFromProducts(List<Long> productIds) {
        List<Product> products = productRepo.findAllById(productIds);
        for (Product product : products) {
            product.setPromotion(null); // Gỡ móc nối
        }
        productRepo.saveAll(products);
    }

    // HÀM PHỤ TRỢ: CHUYÊN BIẾN PRODUCT THÀNH PRODUCT_RESPONSE VÀ TÍNH GIÁ ĐỘNG
    private ProductResponse mapToProductResponse(Product product) {
        BigDecimal originalPrice = product.getPrice();
        BigDecimal salePrice = originalPrice; // Mặc định giá bán = giá gốc
        boolean isPromoted = false;
        String promotionTag = null;

        // Lấy thông tin khuyến mãi của món ăn này
        if (product.getPromotion() != null) {
            // Gọi hàm isValid của Promotion (em đã viết) để check xem KM có đang chạy hợp lệ không
            if (product.getPromotion().isValid(Instant.now())) {
                isPromoted = true;

                // Bắt đầu tính giá
                if (product.getPromotion().getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    // Trừ thẳng tiền
                    salePrice = originalPrice.subtract(product.getPromotion().getDiscountValue());
                    promotionTag = "-" + product.getPromotion().getDiscountValue().stripTrailingZeros().toPlainString() + "đ";
                } else if (product.getPromotion().getDiscountType() == DiscountType.PERCENTAGE) {
                    // Giảm %
                    BigDecimal discountAmount = originalPrice.multiply(product.getPromotion().getDiscountValue()).divide(BigDecimal.valueOf(100));
                    salePrice = originalPrice.subtract(discountAmount);
                    promotionTag = "-" + product.getPromotion().getDiscountValue().stripTrailingZeros().toPlainString() + "%";
                }

                // Nếu giảm lố khiến giá bị âm thì set về 0 (bán tặng)
                if (salePrice.compareTo(BigDecimal.ZERO) < 0) {
                    salePrice = BigDecimal.ZERO;
                }
            }
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(originalPrice)
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .salePrice(salePrice)       // Gắn giá đã tính vào
                .isPromoted(isPromoted)     // Gắn cờ vào
                .promotionTag(promotionTag) // Gắn nhãn vào
                .build();
    }

}
