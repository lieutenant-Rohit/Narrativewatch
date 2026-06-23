package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fired_at", nullable = false)
    private LocalDateTime firedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "account_count", nullable = false)
    private Integer accountCount;

    @Column(nullable = false)
    private Double confidence;

    @Column(name = "signals_fired", nullable = false, length = 100)
    private String signalsFired;
}
