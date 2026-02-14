package com.example.qr_order.controller;

import com.example.qr_order.dtos.CategoryRequest;
import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.CategoryResponse;
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

    @PostMapping()
    public ResponseEntity<ApiResponse<Category>> createdCategory(@RequestBody @Valid CategoryRequest categoryRequest){
        Category newCategory = categoryService.create(categoryRequest);

        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create category successful")
                .data(newCategory)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody @Valid  CategoryRequest categoryRequest ){
        Category updatedCategory = categoryService.update(id, categoryRequest);

        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .status(HttpStatus.OK.value())
                .message("Update category successful")
                .data(updatedCategory)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Category>> deleteCategory (@PathVariable Long id){
        Category deletedCategory = categoryService.delete(id);

        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .status(HttpStatus.OK.value())
                .message("Delete category successful")
                .data(deletedCategory)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategory (){
        List<CategoryResponse> categories = categoryService.getAll();

        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Get all category successful")
                .data(categories)
                .build();
        return ResponseEntity.ok(response);
    }
}
