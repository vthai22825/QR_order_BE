package com.example.qr_order.repository;

import com.example.qr_order.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {

    // THÊM: left join fetch p.promotion pr
    @Query(
            value = "select p from Product p join fetch p.category c left join fetch p.promotion pr where p.isDeleted = false and c.isDeleted = false",
            countQuery = "select count(p) from Product p JOIN p.category c where p.isDeleted = false and c.isDeleted = false"
    )
    Page<Product> findAllActive(Pageable pageable);

    // THÊM: left join fetch p.promotion pr
    @Query(
            value = "select p from Product p join fetch p.category c left join fetch p.promotion pr where p.isDeleted = false and c.isDeleted = false and c.id = :categoryId",
            countQuery = "select count(p) from Product p JOIN p.category c where p.isDeleted = false and c.isDeleted = false and c.id = :categoryId"
    )
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    boolean existsByNameAndIsDeletedFalse(String name);

    // THÊM: LEFT JOIN FETCH p.promotion pr
    @Query("SELECT p FROM Product p JOIN FETCH p.category c LEFT JOIN FETCH p.promotion pr WHERE p.id = :id AND p.isDeleted = false AND c.isDeleted = false")
    Optional<Product> findByIdActive(@Param("id") Long id);

    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);
}