package com.narrativewatch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {
    private Long id;
    private LocalDateTime firedAt;
    private String narrative;
    private int accountCount;
    private double confidence;
    private String signalsFired;
}
