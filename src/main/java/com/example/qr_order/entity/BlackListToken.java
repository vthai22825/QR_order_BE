package com.example.qr_order.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BlackListToken")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlackListToken {

    @Id
    @Column(length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expires_at;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    void on_create(){
        created_at = LocalDateTime.now();
    }

}
