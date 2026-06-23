package com.narrativewatch.repository;

import com.narrativewatch.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountId(String accountId);

    List<AccountEntity> findByBehavioralScoreGreaterThan(Double threshold);
}
