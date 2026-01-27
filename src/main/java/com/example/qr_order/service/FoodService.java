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

import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class FoodService {
    private final FoodRepo foodRepo;
    private final CategoryRepo categoryRepo;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Food createFood(FoodRequest foodRequest, MultipartFile imageFile) {
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
        // Nếu có URL ảnh trong request thì set tạm, sau đó nếu có file upload thì sẽ
        // ghi đè sau
        food.setFoodImage(foodRequest.getFoodImage());
        food.setCategoryId(category);

        Food savedFood = foodRepo.save(food);

        // 4. Xử lý upload ảnh async (Fire & Forget)
        if (imageFile != null && !imageFile.isEmpty()) {
            imageStorageService.uploadImage(imageFile, "Foods", imageUrl -> {
                // Cẩn thận: savedFood đang ở trạng thái detached hoặc managed tùy context.
                // Tốt nhất là fetch lại hoặc save lại instance này.
                // Vì đây là luồng riêng, transaction cũ đã commit.
                // Ta cần lưu lại URL.
                savedFood.setFoodImage(imageUrl);
                foodRepo.save(savedFood);
            });
        }

        return savedFood;
    }

    @Transactional
    public FoodDetailResponse updateFood(Long foodId, FoodRequest request,
            MultipartFile imageFile) {
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

        // Cập nhật text URL nếu có
        if (request.getFoodImage() != null && !request.getFoodImage().isBlank()) {
            food.setFoodImage(request.getFoodImage());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            food.setCategoryId(category);
        }

        Food savedFood = foodRepo.save(food);

        // Xử lý upload ảnh async updates
        if (imageFile != null && !imageFile.isEmpty()) {
            imageStorageService.uploadImage(imageFile, "Foods", imageUrl -> {
                savedFood.setFoodImage(imageUrl);
                foodRepo.save(savedFood);
            });
        }

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
