package com.narrativewatch.detection;

import java.util.List;

public record GraphClusterResult(
    int totalAccounts,
    int suspiciousAccounts,
    double avgCoefficient,
    List<String> flaggedAccountIds,
    boolean signalFired
) {}
