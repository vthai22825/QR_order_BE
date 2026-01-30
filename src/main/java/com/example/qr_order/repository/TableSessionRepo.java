package com.example.qr_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.qr_order.entity.TableSession;
import com.example.qr_order.entity.Tables;
import java.util.Optional;

public interface TableSessionRepo extends JpaRepository<TableSession, Long> {
    Optional<TableSession> findByTables(Tables tables);
}
