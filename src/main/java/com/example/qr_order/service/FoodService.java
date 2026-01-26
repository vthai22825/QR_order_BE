package com.example.qr_order.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qr_order.dtos.FoodRequest;
import com.example.qr_order.entity.Category;
import com.example.qr_order.entity.Food;
import com.example.qr_order.repository.CategoryRepo;
import com.example.qr_order.repository.FoodRepo;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.qr_order.dtos.response.food.FoodDetailResponse;
import com.example.qr_order.dtos.response.food.FoodResponse;

@Service
@AllArgsConstructor
public class FoodService {
    private final FoodRepo foodRepo;
    private final CategoryRepo categoryRepo;

    @Transactional
    public Food createFood(FoodRequest foodRequest) {
        // 1. Xác định đối tượng Food: Tìm food cũ để restore HOẶC tạo mới
        Food food = foodRepo.findByNameIncludingDeleted(foodRequest.getFoodName())
                .map(existing -> {
                    if (!existing.isDeleted()) {
                        throw new RuntimeException(
                                "Food with name '" + foodRequest.getFoodName() + "' already exists!");
                    }
                    existing.setDeleted(false); // Đánh dấu restore
                    return existing;
                })
                .orElse(new Food()); // Nếu không tìm thấy, tạo instance mới

        // 2. Tìm Category
        Category category = categoryRepo.findById(foodRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + foodRequest.getCategoryId()));

        // 3. Cập nhật thông tin
        food.setFoodName(foodRequest.getFoodName());
        food.setFoodDescription(foodRequest.getFoodDescription());
        food.setFoodPrice(foodRequest.getFoodPrice());
        food.setFoodImage(foodRequest.getFoodImage());
        food.setCategoryId(category);

        return foodRepo.save(food);
    }

    @Transactional
    public FoodDetailResponse updateFood(Long foodId, FoodRequest request) {
        Food food = foodRepo.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found with ID: " + foodId));

        if (request.getFoodName() != null && !request.getFoodName().isBlank()) {
            if (!request.getFoodName().equals(food.getFoodName())) {
                Optional<Food> existing = foodRepo.findByNameIncludingDeleted(request.getFoodName());
                if (existing.isPresent()) {
                    throw new RuntimeException("Food with name '" + request.getFoodName() + "' already exists!");
                }
                food.setFoodName(request.getFoodName());
            }
        }

        if (request.getFoodDescription() != null && !request.getFoodDescription().isBlank()) {
            food.setFoodDescription(request.getFoodDescription());
        }

        if (request.getFoodPrice() != null) {
            food.setFoodPrice(request.getFoodPrice());
        }

        if (request.getFoodImage() != null && !request.getFoodImage().isBlank()) {
            food.setFoodImage(request.getFoodImage());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            food.setCategoryId(category);
        }

        Food savedFood = foodRepo.save(food);

        return new FoodDetailResponse(
                savedFood.getFoodId(),
                savedFood.getFoodName(),
                savedFood.getFoodDescription(),
                savedFood.getFoodPrice(),
                savedFood.getFoodImage(),
                new FoodDetailResponse.CategorySummary(
                        savedFood.getCategoryId().getCategoryId(),
                        savedFood.getCategoryId().getCategoryName()),
                savedFood.getCreatedAt(),
                savedFood.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public Page<FoodResponse> getAllFoods(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return foodRepo.findAll(pageable)
                .map(food -> new FoodResponse(
                        food.getFoodId(),
                        food.getFoodName(),
                        food.getFoodDescription(),
                        food.getFoodPrice(),
                        food.getFoodImage(),
                        food.getCategoryId().getCategoryId()));
    }

    @Transactional(readOnly = true)
    public FoodResponse getFoodById(Long foodId) {
        Food food = foodRepo.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found with ID: " + foodId));
        return new FoodResponse(
                food.getFoodId(),
                food.getFoodName(),
                food.getFoodDescription(),
                food.getFoodPrice(),
                food.getFoodImage(),
                food.getCategoryId().getCategoryId());
    }

    @Transactional(readOnly = true)
    public Page<FoodResponse> getFoodsByCategoryId(Long categoryId, int page, int size) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        Pageable pageable = PageRequest.of(page, size);
        return foodRepo.findByCategoryId(category, pageable)
                .map(food -> new FoodResponse(
                        food.getFoodId(),
                        food.getFoodName(),
                        food.getFoodDescription(),
                        food.getFoodPrice(),
                        food.getFoodImage(),
                        food.getCategoryId().getCategoryId()));
    }

    @Transactional
    public void deleteFood(Long foodId) {
        Food food = foodRepo.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found with ID: " + foodId));
        foodRepo.delete(food);
    }

}
