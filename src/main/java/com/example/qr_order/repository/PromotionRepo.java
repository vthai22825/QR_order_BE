package com.example.qr_order.repository;


import com.example.qr_order.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion, Long> {

    //lấy promotion chưa bị xóa
    @Query("select p from Promotion p where p.isDeleted = false")
    Page<Promotion> findByIsDeletedFalse(Pageable pageable);

    //lấy promotion đang hoạt động và chưa bị xóa
    @Query("select p from Promotion p where p.isActive = true and p.isDeleted = false")
    List<Promotion> findByIsActiveTrueAndIsDeletedFalse();

    //lấy chi tiết theo id chưa bị xóa
    @Query("select p from Promotion p where p.isDeleted = false and p.id = :id ")
    Optional<Promotion> findByIdAndIsDeletedFalse(@Param("id") Long id);

    @Query("select p from Promotion p where lower(p.name) like lower(concat('%', :name,'%')) and p.isDeleted = false")
    Page<Promotion> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    boolean existsByNameAndIsDeletedFalse(String name);
}
