package com.example.qr_order.service;


import com.example.qr_order.dtos.CategoryRequest;
import com.example.qr_order.dtos.response.category.CategoryResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.repository.CategoryRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;



    /*
    * Thêm category, kiểm tra xem tên category đã có sẵn chưa
    * nếu có rồi thì sẽ báo là tên đã có
    * nếu tên đó đã bị xóa từ lâu, thì sẽ khôi phục tên đó, vè đặt mô tả mới vì đây là phương thức tạo category
    */
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest){

        if (categoryRepo.existsByCategoryName(categoryRequest.getCategoryName())){
            throw new RuntimeException("Category name already exists: " + categoryRequest.getCategoryName());
        }

        Optional<Category> oldCategory = categoryRepo.findByNameIncludingDeleted(categoryRequest.getCategoryName());

        if (oldCategory.isPresent()){
            Category restoredCategory = oldCategory.get();

            restoredCategory.setDeleted(false);
            restoredCategory.setCategoryDescription(categoryRequest.getCategoryDescription());

            return categoryRepo.save(restoredCategory);
        }

        Category newCategory = new Category();

        newCategory.setCategoryName(categoryRequest.getCategoryName());
        newCategory.setCategoryDescription(categoryRequest.getCategoryDescription());

        return categoryRepo.save(newCategory);
    }



    /*sửa category, chỉ sửa khi có sự thay đổi, cái nào không thay đổi thì giữ nguyên*/
    @Transactional
    public Category updateCategory(Long categoryId, CategoryRequest request) {
        Category existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        if (request.getCategoryName() != null) {
            String newName = request.getCategoryName();
            String oldName = existingCategory.getCategoryName();

            if (!newName.equals(oldName)) {
                if (categoryRepo.findByNameIncludingDeleted(newName).isPresent()) {
                    throw new RuntimeException("Category name already exists: " + newName);
                }

                existingCategory.setCategoryName(newName);
            }
        }

        if (request.getCategoryDescription() != null) {
            String newDescription = request.getCategoryDescription();
            String oldDescription = existingCategory.getCategoryDescription();

            if (!newDescription.equals(oldDescription)) {
                existingCategory.setCategoryDescription(newDescription);
            }
        }

        return categoryRepo.save(existingCategory);
    }




    // lấy toàn bộ category
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        List<Category> categories = categoryRepo.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));

        return categories.stream()
                .map(category -> new CategoryResponse(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getCategoryDescription()
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        categoryRepo.delete(category);
    }
}
