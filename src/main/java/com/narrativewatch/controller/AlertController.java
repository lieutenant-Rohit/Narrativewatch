package com.narrativewatch.controller;

import com.narrativewatch.dto.AlertResponse;
import com.narrativewatch.entity.AlertEntity;
import com.narrativewatch.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertResponse>> getAlerts() {
        List<AlertEntity> alerts = alertRepository.findAllByOrderByFiredAtDesc();
        List<AlertResponse> response = alerts.stream()
                .map(a -> AlertResponse.builder()
                        .id(a.getId())
                        .firedAt(a.getFiredAt())
                        .narrative(a.getNarrative())
                        .accountCount(a.getAccountCount())
                        .confidence(a.getConfidence())
                        .signalsFired(a.getSignalsFired())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }
}
