package com.example.qr_order.entity;

import com.example.qr_order.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "promotion")
@DynamicUpdate
public class Promotion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long id;

    @Column(name = "promotion_name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "promotion_image")
    private String promotionImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value")
    @NotNull
    private BigDecimal discountValue;

    @Column(name = "start_date")
    @NotNull
    private Instant startDate;

    @Column(name = "end_date")
    @NotNull
    private Instant endDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public boolean isValid(Instant timeToCheck){
        if (isDeleted || !isActive) return false;
        if (timeToCheck.isBefore(startDate)) return false;
        if (timeToCheck.isAfter(endDate)) return false;
        return true;
    }

    public void setName(String name){this.name = name;}

    public void setDescription(String description) {
        this.description = description;
    }
    public void setPromotionImage(String promotionImage){this.promotionImage = promotionImage;}
    public void setDiscountType(DiscountType discountType){this.discountType = discountType;}
    public void setDiscountValue(BigDecimal discountValue){
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Discount value cannot be negative");
        }
        this.discountValue = discountValue;
    }
    public void setStartDate(Instant startDate){this.startDate = startDate;}
    public void setEndDate(Instant endDate){
        if (endDate.isBefore(startDate)){
            throw new RuntimeException("End date cannot be before start date");
        }
        this.endDate = endDate;
    }
    public void setActive(boolean isActive){this.isActive = isActive;}
    public void setDeleted(boolean isDeleted){this.isDeleted = isDeleted;}
}
