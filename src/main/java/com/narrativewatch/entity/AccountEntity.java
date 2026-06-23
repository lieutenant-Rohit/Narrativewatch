package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true, length = 100)
    private String accountId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "avg_interval_sec")
    private Double avgIntervalSec;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "follower_count")
    private Integer followerCount;

    @Column(name = "topic_history", columnDefinition = "TEXT")
    private String topicHistory;

    @Column(name = "behavioral_score")
    private Double behavioralScore;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "is_active")
    private Boolean isActive;
}
