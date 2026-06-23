package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_account", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alert_id", "account_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;
}
