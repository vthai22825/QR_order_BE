package com.example.qr_order.entity;

import com.example.qr_order.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(nullable = false, unique = true, length = 50)
    private String user_name;

    @Column(nullable = false, length = 255)
    private String password_hash;

    @Column(nullable = false, length = 100)
    private String full_name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean is_active = true;

    @Column(nullable = false, updatable = false)
    private Instant created_at;

    @Column(nullable = false)
    private Instant updated_at;

    @PrePersist
    protected void on_create() {
        Instant now = Instant.now();
        this.created_at = now;
        this.updated_at = now;
    }

    @PreUpdate
    protected void on_update() {
        this.updated_at = Instant.now();
    }
}
