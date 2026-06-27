package com.narrativewatch.detection;

import java.time.LocalDateTime;

public record TemporalClusterResult(
    String topic,
    LocalDateTime windowStart,
    long postCount,
    double avgOtherTopics,
    double spikeRatio,
    boolean signalFired
) {}
