package com.example.qr_order.service;

import com.example.qr_order.dtos.CategoryRequest;
import com.example.qr_order.dtos.response.CategoryResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.repository.CategoryRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;


    @Transactional
    public Category create (CategoryRequest categoryRequest){

        if (categoryRequest == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be null");
        }
        Optional<Category> existingOpt = categoryRepo.findByName(categoryRequest.getName());

        if (existingOpt.isPresent()){
            Category exstingCategory = existingOpt.get();

            if (exstingCategory.isDeleted()){
                exstingCategory.setDeleted(false);
                exstingCategory.setDescription(categoryRequest.getDescription());

                return categoryRepo.save(exstingCategory);
            }
            else {
                throw new RuntimeException(categoryRequest.getName()+ "already exist");
            }
        }
        Category newCategory = new Category();
        newCategory.setName(categoryRequest.getName());
        newCategory.setDescription(categoryRequest.getDescription());

        return categoryRepo.save(newCategory);

    }


    @Transactional
    public Category update (Long id, CategoryRequest categoryRequest){

        Category existing = categoryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: "+ id));

        if (!existing.getName().equals(categoryRequest.getName())){
            if (categoryRepo.existsByName(categoryRequest.getName())){
                throw new ResponseStatusException(HttpStatus.CONFLICT, categoryRequest.getName() + " already exist");
            }
            existing.setName(categoryRequest.getName());
        }

        if (categoryRequest.getDescription() != null){
            existing.setDescription(categoryRequest.getDescription());
        }

        return categoryRepo.save(existing);
    }

    @Transactional
    public Category delete (Long id){
        Category existing = categoryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id: "+ id));

        existing.setDeleted(true);

        return categoryRepo.save(existing);
    }

    public List<CategoryResponse> getAll (){
        List<Category> categories = categoryRepo.findByIsDeletedFalse();

        return categories.stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .toList();
    }
}
