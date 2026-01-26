package com.example.qr_order.controller;

import com.example.qr_order.dtos.FoodRequest;
import com.example.qr_order.dtos.response.food.FoodDetailResponse;
import com.example.qr_order.dtos.response.food.FoodResponse;
import com.example.qr_order.entity.Food;
import com.example.qr_order.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    // Create - Add new food
    @PostMapping("/add")
    public ResponseEntity<Food> createFood(@Valid @RequestBody FoodRequest request) {
        Food savedFood = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFood);
    }

    // Update - Update food
    @PutMapping("/update/{id}")
    public ResponseEntity<FoodDetailResponse> updateFood(@Valid @PathVariable("id") Long foodId,
            @RequestBody FoodRequest request) {
        FoodDetailResponse updatedFood = foodService.updateFood(foodId, request);
        return ResponseEntity.ok(updatedFood);
    }

    // Get All - Retrieve all active foods
    @GetMapping("/all")
    public ResponseEntity<Page<FoodResponse>> getAllFoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(foodService.getAllFoods(page, size));
    }

    // Get By Id - Retrieve a specific food by ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    // Get By Category Id - Retrieve foods by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(foodService.getFoodsByCategoryId(categoryId, page, size));
    }

    // Delete - Soft delete food
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable("id") Long foodId) {
        foodService.deleteFood(foodId);
        return ResponseEntity.noContent().build();
    }

}
