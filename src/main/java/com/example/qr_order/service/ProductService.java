package com.example.qr_order.service;

import com.example.qr_order.dtos.ProductRequest;
import com.example.qr_order.dtos.response.PageResponse;
import com.example.qr_order.dtos.response.ProductResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.entity.Product;
import com.example.qr_order.repository.CategoryRepo;
import com.example.qr_order.repository.ProductRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ImageStorageService imageStorageService;

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
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getImageUrl(),
                        product.getPrice(),

                        product.getCategory().getId(),
                        product.getCategory().getName()))
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

    public PageResponse<ProductResponse> getByCategoryId(Long categoryId, int page, int size) {

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        
        if(category.isDeleted()) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category has been deleted");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepo.findByCategoryId(categoryId, pageable);

        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getImageUrl(),
                        product.getPrice(),
                        product.getCategory().getId(),
                        product.getCategory().getName()
                ))
                .toList();

        return PageResponse.<ProductResponse>builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .total(productPage.getTotalElements())
                .items(productResponses)
                .build();
    }

}
