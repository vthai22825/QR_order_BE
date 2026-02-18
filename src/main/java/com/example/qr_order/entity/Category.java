package com.example.qr_order.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor
@DynamicUpdate
public class Category extends BaseAuditEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, length = 30, unique = true)
    private String name;

    @Column(name = "category_description", length = 400)
    private String description;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Version
    private Integer version;

    public void setName(String name){
        if (name == null){
            throw new IllegalArgumentException("Category name cannot be null");
        }
        this.name = name.trim();
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setDeleted(boolean isDeleted){
        this.isDeleted = isDeleted;
    }

}
