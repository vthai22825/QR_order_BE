package com.example.qr_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.qr_order.entity.Tables;
import java.util.Optional;

public interface TableRepo extends JpaRepository<Tables, Long> {
    Optional<Tables> findByTableName(String tableName);
}
