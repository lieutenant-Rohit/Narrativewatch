package com.narrativewatch.detection;

import com.narrativewatch.entity.TemporalBucketEntity;
import com.narrativewatch.repository.TemporalBucketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemporalClusterScorer {

    private static final Logger log = LoggerFactory.getLogger(TemporalClusterScorer.class);
    private static final double SPIKE_THRESHOLD = 3.0;
    private static final int LOOKBACK_MINUTES = 10;

    private final TemporalBucketRepository temporalBucketRepository;

    public List<TemporalClusterResult> evaluate() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOOKBACK_MINUTES);
        List<TemporalBucketEntity> buckets = temporalBucketRepository.findByWindowStartAfter(since);

        if (buckets.isEmpty()) {
            log.info("Temporal scorer: no buckets in last {} minutes", LOOKBACK_MINUTES);
            return List.of();
        }

        Map<LocalDateTime, List<TemporalBucketEntity>> byWindow = buckets.stream()
                .collect(Collectors.groupingBy(TemporalBucketEntity::getWindowStart));

        List<TemporalClusterResult> results = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<TemporalBucketEntity>> entry : byWindow.entrySet()) {
            LocalDateTime window = entry.getKey();
            List<TemporalBucketEntity> windowBuckets = entry.getValue();

            double totalPostCount = windowBuckets.stream()
                    .mapToLong(b -> b.getPostCount() != null ? b.getPostCount() : 0)
                    .sum();
            long topicCount = windowBuckets.size();
            double avgPerTopic = topicCount > 0 ? totalPostCount / topicCount : 0;

            for (TemporalBucketEntity bucket : windowBuckets) {
                long count = bucket.getPostCount() != null ? bucket.getPostCount() : 0;
                double otherAvg = topicCount > 1
                        ? (totalPostCount - count) / (topicCount - 1)
                        : 0;
                double spikeRatio = otherAvg > 0 ? count / otherAvg : (count > 0 ? Double.MAX_VALUE : 0);
                boolean fired = spikeRatio >= SPIKE_THRESHOLD;

                results.add(new TemporalClusterResult(
                        bucket.getTopic(), window, count, otherAvg, spikeRatio, fired));

                if (fired) {
                    log.warn("Temporal signal fired: topic={} window={} count={} spike={}x",
                            bucket.getTopic(), window, count, String.format("%.1f", spikeRatio));
                }
            }
        }

        long fired = results.stream().filter(TemporalClusterResult::signalFired).count();
        log.info("Temporal scorer: {} windows checked, {} signals fired", results.size(), fired);
        return results;
    }
}
