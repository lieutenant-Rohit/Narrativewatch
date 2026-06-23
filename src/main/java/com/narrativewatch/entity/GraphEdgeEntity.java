package com.narrativewatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "graph_edge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"from_account", "to_account"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraphEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_account", nullable = false, length = 100)
    private String fromAccount;

    @Column(name = "to_account", nullable = false, length = 100)
    private String toAccount;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;
}
