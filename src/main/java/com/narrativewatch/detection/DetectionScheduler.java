package com.narrativewatch.detection;

import com.narrativewatch.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DetectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DetectionScheduler.class);

    private final TemporalClusterScorer temporalScorer;
    private final GraphScorer graphScorer;
    private final DetectionService detectionService;
    private final CoordinationDetector coordinationDetector;

    @Scheduled(fixedRate = 30000)
    public void runDetection() {
        log.info("Detection cycle starting");

        var temporalResults = temporalScorer.evaluate();
        var graphResult = graphScorer.evaluate();
        boolean behavioral = detectionService.runBehavioral();
        boolean semantic = detectionService.runSemantic();

        long temporalFired = temporalResults.stream()
                .filter(TemporalClusterResult::signalFired)
                .count();

        log.info("Scoring complete: temporal={}/{} graph={} behavioral={} semantic={}",
                temporalFired, temporalResults.size(),
                graphResult.signalFired(), behavioral, semantic);

        coordinationDetector.evaluate(temporalResults, graphResult, behavioral, semantic);
    }
}
