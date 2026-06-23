package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "topic_bucket", length = 50)
    private String topicBucket;

    @Column(name = "semantic_score")
    private Double semanticScore;
}
