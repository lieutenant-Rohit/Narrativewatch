package com.narrativewatch.repository;

import com.narrativewatch.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findByAccountId(String accountId);

    List<PostEntity> findByTopicBucketAndPostedAtAfter(String topic, LocalDateTime after);

    long countByTopicBucketAndPostedAtBetween(String topic, LocalDateTime start, LocalDateTime end);
}
