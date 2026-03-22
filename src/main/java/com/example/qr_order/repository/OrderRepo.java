package com.example.qr_order.repository;


import com.example.qr_order.entity.Order;
import com.example.qr_order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {


    @Query("select o from Order o join fetch o.table t where t.id = :tableId and o.status = :status")
    Optional<Order> findByTableIdAndStatusWithTable(
            @Param("tableId") Long tableId,
            @Param("status")OrderStatus status
    );

    @Query("select o from Order o join fetch o.table where o.id = :orderId")
    Optional<Order> findByIdWithTable(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status = 'UNPAID'")
    Optional<Order> findActiveOrderByTableId(@Param("tableId") Long tableId);

    // ==================== THỐNG KÊ DOANH THU ====================

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND FUNCTION('DATE', o.createdAt) = :date")
    BigDecimal sumRevenueByDay(@Param("date") LocalDate date);

    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND FUNCTION('DATE', o.createdAt) = :date")
    long countOrdersByDay(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    BigDecimal sumRevenueByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    long countOrdersByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND YEAR(o.createdAt) = :year")
    BigDecimal sumRevenueByYear(@Param("year") int year);

    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND YEAR(o.createdAt) = :year")
    long countOrdersByYear(@Param("year") int year);
}
