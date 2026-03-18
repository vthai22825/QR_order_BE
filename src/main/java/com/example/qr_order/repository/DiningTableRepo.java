package com.example.qr_order.repository;

import com.example.qr_order.entity.DiningTable;
import com.example.qr_order.enums.TableStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningTableRepo extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findAllByIsDeletedFalseOrderByIdDesc();
    List<DiningTable> findAllByIsDeletedFalseAndStatusOrderByIdDesc(TableStatus status);
    Page<DiningTable> findAllByIsDeletedFalse(Pageable pageable);
    Optional<DiningTable> findByIdAndIsDeletedFalse(Long id);
    boolean existsByNameAndIsDeletedFalse(String name);
}
