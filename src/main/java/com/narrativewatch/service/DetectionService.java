package com.narrativewatch.service;

import com.narrativewatch.entity.AccountEntity;
import com.narrativewatch.entity.PostEntity;
import com.narrativewatch.repository.AccountRepository;
import com.narrativewatch.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DetectionService {

    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    private static final double BEHAVIORAL_THRESHOLD = 0.8;
    private static final int SEMANTIC_CLUSTER_THRESHOLD = 3;
    private static final int LOOKBACK_MINUTES = 10;

    private final RestClient mlRestClient;
    private final AccountRepository accountRepository;
    private final PostRepository postRepository;

    public boolean runBehavioral() {
        List<AccountEntity> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            log.info("Behavioral scoring: no accounts");
            return false;
        }

        List<BehavioralRequest> request = accounts.stream()
                .map(a -> new BehavioralRequest(
                        a.getAccountId(),
                        a.getAvgIntervalSec() != null ? a.getAvgIntervalSec() : 0.0,
                        a.getPostCount() != null ? a.getPostCount() : 0,
                        a.getFollowerCount() != null ? a.getFollowerCount() : 0,
                        a.getTopicHistory() != null && !a.getTopicHistory().isEmpty()
                                ? a.getTopicHistory().split(",").length : 0))
                .toList();

        BehavioralResponse[] responses;
        try {
            responses = mlRestClient.post()
                    .uri("/ml/behavioral")
                    .body(request)
                    .retrieve()
                    .body(BehavioralResponse[].class);
        } catch (Exception e) {
            log.error("Behavioral ML call failed: {}", e.getMessage());
            return false;
        }

        if (responses == null) return false;

        for (BehavioralResponse resp : responses) {
            accountRepository.findByAccountId(resp.accountId()).ifPresent(account -> {
                account.setBehavioralScore(resp.anomalyScore());
                accountRepository.save(account);
            });
        }

        long highScoreCount = Arrays.stream(responses)
                .filter(r -> r.anomalyScore() > BEHAVIORAL_THRESHOLD)
                .count();

        log.info("Behavioral scoring: {} accounts, {} above threshold",
                responses.length, highScoreCount);
        return highScoreCount > 0;
    }

    public boolean runSemantic() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOOKBACK_MINUTES);
        List<PostEntity> posts = postRepository.findByPostedAtAfter(since);
        if (posts.isEmpty()) {
            log.info("Semantic scoring: no posts in last {} minutes", LOOKBACK_MINUTES);
            return false;
        }

        List<SemanticRequest> request = posts.stream()
                .map(p -> new SemanticRequest(
                        p.getId().toString(),
                        p.getText()))
                .toList();

        SemanticResponse[] responses;
        try {
            responses = mlRestClient.post()
                    .uri("/ml/semantic")
                    .body(request)
                    .retrieve()
                    .body(SemanticResponse[].class);
        } catch (Exception e) {
            log.error("Semantic ML call failed: {}", e.getMessage());
            return false;
        }

        if (responses == null) return false;

        for (SemanticResponse resp : responses) {
            try {
                Long postId = Long.parseLong(resp.postId());
                postRepository.findById(postId).ifPresent(post -> {
                    post.setSemanticScore(resp.similarityScore());
                    postRepository.save(post);
                });
            } catch (NumberFormatException ignored) {}
        }

        long clusteredCount = Arrays.stream(responses)
                .filter(r -> r.clusterId() >= 0)
                .count();

        long groupCount = Arrays.stream(responses)
                .filter(r -> r.clusterId() >= 0)
                .map(SemanticResponse::clusterId)
                .distinct()
                .count();

        boolean signalFired = groupCount > 0 && clusteredCount >= SEMANTIC_CLUSTER_THRESHOLD;
        log.info("Semantic scoring: {} posts, {} clustered into {} groups, signal={}",
                responses.length, clusteredCount, groupCount, signalFired);
        return signalFired;
    }

    private record BehavioralRequest(
            String accountId, double avgIntervalSec,
            int postCount, int followerCount, int topicCount) {}
    private record BehavioralResponse(String accountId, double anomalyScore) {}
    private record SemanticRequest(String postId, String text) {}
    private record SemanticResponse(String postId, int clusterId, double similarityScore) {}
}
