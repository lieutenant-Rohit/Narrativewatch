package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporal_bucket", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"topic", "window_start"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporalBucketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String topic;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "suspicious_count")
    private Integer suspiciousCount;
}
