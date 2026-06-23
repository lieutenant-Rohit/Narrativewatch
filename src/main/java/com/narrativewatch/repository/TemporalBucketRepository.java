package com.narrativewatch.repository;

import com.narrativewatch.entity.TemporalBucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporalBucketRepository extends JpaRepository<TemporalBucketEntity, Long> {

    Optional<TemporalBucketEntity> findByTopicAndWindowStart(String topic, LocalDateTime windowStart);
}
