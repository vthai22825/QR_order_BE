package com.example.qr_order.repository;

import com.example.qr_order.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    boolean existsByCategoryName(String categoryName);


    @Query(value = "SELECT * FROM category WHERE category_name = :name", nativeQuery = true)
    Optional<Category> findByNameIncludingDeleted(@Param("name") String categoryName);


}
