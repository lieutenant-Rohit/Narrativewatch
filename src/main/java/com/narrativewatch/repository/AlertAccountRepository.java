package com.narrativewatch.repository;

import com.narrativewatch.entity.AlertAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertAccountRepository extends JpaRepository<AlertAccountEntity, Long> {

    List<AlertAccountEntity> findByAlertId(Long alertId);
}
