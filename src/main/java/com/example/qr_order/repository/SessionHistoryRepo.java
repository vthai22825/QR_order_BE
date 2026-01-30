package com.example.qr_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.qr_order.entity.SessionHistory;
import java.util.List;

public interface SessionHistoryRepo extends JpaRepository<SessionHistory, Long> {
    List<SessionHistory> findByTableId(Long tableId);
}
