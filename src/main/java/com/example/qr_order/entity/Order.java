package com.example.qr_order.entity;

import com.example.qr_order.enums.OrderStatus;
import com.example.qr_order.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private DiningTable table;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.UNPAID;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "note",length = 500)
    private String note;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    public void setTable(DiningTable table){
        this.table = table;
    }
    public void setStatus(OrderStatus status){
        this.status = status;
    }
    public void setTotalPrice(BigDecimal totalPrice){
        this.totalPrice = totalPrice;
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setPaymentMethod(PaymentMethod paymentMethod){this.paymentMethod = paymentMethod;}
}
