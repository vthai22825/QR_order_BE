package com.example.qr_order.controller;

import com.example.qr_order.dtos.CategoryRequest;
import com.example.qr_order.dtos.response.category.CategoryResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Create - Add new category
    @PostMapping("/add")
    public ResponseEntity<CategoryResponse> createdCategory(@Valid @RequestBody CategoryRequest request) {

        Category savedCategory = categoryService.createCategory(request);

        CategoryResponse response = new CategoryResponse(
                savedCategory.getCategoryId(),
                savedCategory.getCategoryName(),
                savedCategory.getCategoryDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Read - Get all categories
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Update - Update category by id
    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryRequest request) {

        Category updatedCategory = categoryService.updateCategory(categoryId, request);

        CategoryResponse response = new CategoryResponse(
                updatedCategory.getCategoryId(),
                updatedCategory.getCategoryName(),
                updatedCategory.getCategoryDescription());
        return ResponseEntity.ok(response);
    }

    // Delete - Delete category by id
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully");
    }

}
