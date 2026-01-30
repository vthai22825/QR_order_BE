package com.example.qr_order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "table_sessions")
public class TableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Tables tables;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "current_total_amount")
    private double currentTotalAmount = 0.0;

    @PrePersist
    protected void onCreate() {
        this.startTime = Instant.now();
    }
}
