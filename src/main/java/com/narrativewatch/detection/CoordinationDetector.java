package com.narrativewatch.detection;

import com.narrativewatch.entity.AlertAccountEntity;
import com.narrativewatch.entity.AlertEntity;
import com.narrativewatch.repository.AlertAccountRepository;
import com.narrativewatch.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CoordinationDetector {

    private static final Logger log = LoggerFactory.getLogger(CoordinationDetector.class);
    private static final int SIGNAL_THRESHOLD = 2;

    private final AlertRepository alertRepository;
    private final AlertAccountRepository alertAccountRepository;

    public void evaluate(List<TemporalClusterResult> temporalResults, GraphClusterResult graphResult) {
        boolean temporalFired = temporalResults.stream().anyMatch(TemporalClusterResult::signalFired);
        boolean graphFired = graphResult.signalFired();

        int signalsFired = 0;
        StringBuilder signalsStr = new StringBuilder();
        if (temporalFired) {
            signalsFired++;
            signalsStr.append("temporal");
        }
        if (graphFired) {
            if (signalsStr.length() > 0) signalsStr.append(", ");
            signalsFired++;
            signalsStr.append("network");
        }

        if (signalsFired < SIGNAL_THRESHOLD) return;

        String topic = temporalResults.stream()
                .filter(TemporalClusterResult::signalFired)
                .findFirst()
                .map(TemporalClusterResult::topic)
                .orElse("unknown");

        String narrative = "Potential coordinated narrative injection detected in topic '" + topic + "'";
        int accountCount = graphFired ? graphResult.suspiciousAccounts() : 0;
        double confidence = Math.min((double) signalsFired / 2.0, 1.0);
        String firedSignals = signalsStr.toString();

        if (isDuplicate(narrative)) {
            log.info("Suppressed duplicate alert for: {}", narrative);
            return;
        }

        AlertEntity alert = AlertEntity.builder()
                .firedAt(LocalDateTime.now())
                .narrative(narrative)
                .accountCount(accountCount)
                .confidence(confidence)
                .signalsFired(firedSignals)
                .build();
        alertRepository.save(alert);

        for (String accountId : graphResult.flaggedAccountIds()) {
            alertAccountRepository.save(AlertAccountEntity.builder()
                    .alertId(alert.getId())
                    .accountId(accountId)
                    .build());
        }

        log.warn("ALERT FIRED: {} | accounts={} confidence={} signals=[{}]",
                narrative, accountCount, String.format("%.2f", confidence), firedSignals);
    }

    private boolean isDuplicate(String narrative) {
        List<AlertEntity> recent = alertRepository.findAllByOrderByFiredAtDesc();
        return recent.stream()
                .limit(10)
                .anyMatch(a -> a.getNarrative().equals(narrative)
                        && a.getFiredAt().isAfter(LocalDateTime.now().minusMinutes(5)));
    }
}
