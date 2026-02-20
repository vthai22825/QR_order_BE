package com.example.qr_order.entity;

import com.example.qr_order.enums.TableStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "dining_table")
@Getter
@NoArgsConstructor
public class DiningTable extends BaseAuditEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    @Column(nullable = false, length = 300, name = "table_name")
    private String name;

    @Column(name = "table_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        this.name = name.trim();
    }
    public void setStatus(TableStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Table status cannot be null");
        }
        this.status = status;
    }
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
