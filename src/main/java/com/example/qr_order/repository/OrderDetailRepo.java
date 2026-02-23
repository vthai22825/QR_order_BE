package com.example.qr_order.repository;

import com.example.qr_order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepo extends JpaRepository<OrderDetail, Long> {

    @Query("select od from OrderDetail od join fetch od.product p where od.order.id = :orderId")
    List<OrderDetail> findByOrderIdWithProduct(@Param("orderId") Long orderId);

    Optional<OrderDetail> findByOrderIdAndProductId(Long orderId, Long productId);
}
