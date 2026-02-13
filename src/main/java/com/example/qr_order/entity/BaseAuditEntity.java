package com.example.qr_order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass //đánh giấu là class cha
@EntityListeners(AuditingEntityListener.class)//lắng nghe sự kiện khi có thay đổi save/update

public class BaseAuditEntity {
    @CreatedDate //tự động lấy thời gian hiện tại khi insert
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate //tự động lấy thời gian hiện tại khi update
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
