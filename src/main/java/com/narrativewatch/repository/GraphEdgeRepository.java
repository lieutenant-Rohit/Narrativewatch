package com.narrativewatch.repository;

import com.narrativewatch.entity.GraphEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdgeEntity, Long> {

    List<GraphEdgeEntity> findByFromAccount(String fromAccount);

    List<GraphEdgeEntity> findByToAccount(String toAccount);
}
