package com.example.qr_order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.example.qr_order.entity.Category;
import com.example.qr_order.entity.Food;

public interface FoodRepo extends JpaRepository<Food, Long> {

    boolean existsByFoodName(String foodName);

    @Query(value = "SELECT * FROM food WHERE food_name = :foodName", nativeQuery = true)
    Optional<Food> findByNameIncludingDeleted(@Param("foodName") String foodName);

    List<Food> findByCategoryId(Category categoryId);

    Page<Food> findByCategoryId(Category category, Pageable pageable);
}