package com.example.qr_order.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "product")
@DynamicUpdate
public class Product extends BaseAuditEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name", length = 200, nullable = false)
    private String name;

    @Column(name = "product_description", length = 400)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "product_price", nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    public void setName(String name){

        if (name == null){
            throw new IllegalArgumentException("Product name cannot be null");
        }
        this.name = name.trim();
    }

    public void setDescription(String description){
        this.description = description;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public void setDeleted(boolean isDeleted){
        this.isDeleted = isDeleted;
    }
    public void setPrice(BigDecimal price){
        if (price == null){
            throw new IllegalArgumentException("Price can not be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Price cannot be nagative");
        }
        this.price = price;
    }
    public void setCategory(Category category){
        this.category = category;
    }
}
