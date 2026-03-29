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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ImageStorageService imageStorageService;
    private final PromotionRepo promotionRepo;

    @Transactional
    public Product create(ProductRequest productRequest, MultipartFile file) {
        Category category = categoryRepo.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "category with id " + productRequest.getCategoryId() + " not found"));

        if (category.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create product with deleted category");
        }


        String productName = productRequest.getName().trim();

        if (productRepo.existsByNameAndIsDeletedFalse(productName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product name '" + productName + "' already exists");
        }

        Product newProduct = new Product();
        newProduct.setName(productName);
        newProduct.setDescription(productRequest.getDescription());
        newProduct.setPrice(productRequest.getPrice());
        newProduct.setCategory(category);

        if (file != null && !file.isEmpty()) {
            String imageUrl = imageStorageService.uploadImageSync(file, "product");
            newProduct.setImageUrl(imageUrl);
        }

        return productRepo.save(newProduct);
    }

    @Transactional
    public Product update(Long id, ProductRequest productRequest, MultipartFile file) {

        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 1. Cập nhật Category
        if (productRequest.getCategoryId() != null
                && !productRequest.getCategoryId().equals(existing.getCategory().getId())) {

            Category newCategory = categoryRepo.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

            if (newCategory.isDeleted()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move to a deleted category");
            }

            existing.setCategory(newCategory);
        }

        // 2. Cập nhật Tên món (Có trim() cho an toàn)
        if (productRequest.getName() != null && !productRequest.getName().trim().isEmpty()) {
            String newName = productRequest.getName().trim();
            if (!newName.equals(existing.getName())) {
                if (productRepo.existsByNameAndIsDeletedFalse(newName)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists");
                }
                existing.setName(newName);
            }
        }

        // 3. Cập nhật Mô tả
        if (productRequest.getDescription() != null) {
            existing.setDescription(productRequest.getDescription());
        }

        // 4. Cập nhật Giá
        if (productRequest.getPrice() != null) {
            existing.setPrice(productRequest.getPrice());
        }

        if (file != null && !file.isEmpty()) {
            String imageUrl = imageStorageService.uploadImageSync(file, "products");
            existing.setImageUrl(imageUrl);
        }

        return productRepo.save(existing);
    }

    public List<ProductResponse> getAllProducts() {
        // Vẫn giữ nguyên chiến thuật sắp xếp: Category trước, Món mới sau
        Sort sort = Sort.by(
                Sort.Order.asc("category.id"),
                Sort.Order.desc("id")
        );

        // Gọi DB lấy TẤT CẢ trong 1 nốt nhạc
        List<Product> products = productRepo.findByIsDeletedFalse(sort);

        // Map sang DTO (đã bao gồm tính giá khuyến mãi, làm tròn...)
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getByCategoryId(Long categoryId) {

        // 1. Kiểm tra Category có tồn tại và chưa bị xóa không
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (category.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category has been deleted");
        }

        // 2. Tạo quy tắc sắp xếp: Trong cùng 1 danh mục, đưa món Best Seller lên trước, sau đó xếp theo món mới nhất (ID giảm dần)
        Sort sort = Sort.by(
                Sort.Order.desc("isBestSeller"),
                Sort.Order.desc("id")
        );

        // 3. Gọi DB lấy 1 phát ra hết luôn cái List
        List<Product> products = productRepo.findByCategoryId(categoryId, sort);

        // 4. Map sang DTO và trả về
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList()); // Dùng .toList() nếu em xài Java 16+
    }

    public List<ProductResponse> getByPromotionId(Long promotionId) {
        // Kiểm tra Promotion có tồn tại không
        Promotion promotion = promotionRepo.findByIdAndIsDeletedFalse(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));

        Sort sort = Sort.by(
                Sort.Order.desc("isBestSeller"),
                Sort.Order.desc("id")
        );

        List<Product> products = productRepo.findByPromotionIdAndIsDeletedFalse(promotionId, sort);

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
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


    public List<ProductResponse> searchProduct(String keyword){

        if (keyword == null && keyword.trim().isEmpty()){
            return new ArrayList<>();
        }
        List<Product> products = productRepo.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword.trim());

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateBestSellerStatus(Long id, boolean status) {
        // 1. Tìm món ăn
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 2. Cập nhật trạng thái Best Seller theo tham số truyền vào
        product.setBestSeller(status);

        // 3. Lưu xuống Database
        Product savedProduct = productRepo.save(product);

        // 4. Trả về Response xịn xò có tính toán luôn giá Sale
        return mapToProductResponse(savedProduct);
    }

    public List<ProductResponse> getBestSellers() {
        List<Product> bestSellers = productRepo.findByIsBestSellerTrueAndIsDeletedFalse();

        return bestSellers.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
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

        // fix sửa lỗi làm tròn xuống
        salePrice = salePrice.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(1000));

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
