package com.narrativewatch.service;

import com.narrativewatch.dto.PostRequest;
import com.narrativewatch.entity.*;
import com.narrativewatch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final TemporalBucketRepository temporalBucketRepository;

    @Transactional
    public void ingestPost(PostRequest request) {
        AccountEntity account = findOrCreateAccount(request);
        updateAccountBehavior(account, request);

        PostEntity post = savePost(request, account);

        saveGraphEdges(request, account);

        updateTemporalBucket(request);
    }

    private AccountEntity findOrCreateAccount(PostRequest request) {
        return accountRepository.findByAccountId(request.getAccountId())
                .orElseGet(() -> accountRepository.save(
                        AccountEntity.builder()
                                .accountId(request.getAccountId())
                                .createdAt(LocalDateTime.now())
                                .postCount(0)
                                .followerCount(0)
                                .avgIntervalSec(0.0)
                                .topicHistory("")
                                .behavioralScore(0.0)
                                .isActive(true)
                                .build()
                ));
    }

    private void updateAccountBehavior(AccountEntity account, PostRequest request) {
        LocalDateTime now = request.getPostedAt();

        if (account.getLastSeen() != null) {
            long gap = ChronoUnit.SECONDS.between(account.getLastSeen(), now);
            double currentAvg = account.getAvgIntervalSec() != null ? account.getAvgIntervalSec() : 0.0;
            long currentCount = account.getPostCount() != null ? account.getPostCount() : 0;
            double newAvg = ((currentAvg * currentCount) + gap) / (currentCount + 1);
            account.setAvgIntervalSec(newAvg);
        }

        account.setPostCount(account.getPostCount() != null ? account.getPostCount() + 1 : 1);
        account.setLastSeen(now);

        Set<String> topics = new HashSet<>();
        if (account.getTopicHistory() != null && !account.getTopicHistory().isEmpty()) {
            topics.addAll(Arrays.asList(account.getTopicHistory().split(",")));
        }
        if (request.getTopicBucket() != null) {
            topics.add(request.getTopicBucket());
        }
        account.setTopicHistory(String.join(",", topics));

        account.setFollowerCount(request.getFollows() != null ? request.getFollows().size() : 0);

        accountRepository.save(account);
    }

    private PostEntity savePost(PostRequest request, AccountEntity account) {
        PostEntity post = PostEntity.builder()
                .accountId(account.getAccountId())
                .text(request.getText())
                .postedAt(request.getPostedAt())
                .topicBucket(request.getTopicBucket() != null ? request.getTopicBucket() : "general")
                .semanticScore(0.0)
                .build();
        return postRepository.save(post);
    }

    private void saveGraphEdges(PostRequest request, AccountEntity account) {
        if (request.getFollows() == null) return;

        for (String followedAccount : request.getFollows()) {
            if (followedAccount.equals(account.getAccountId())) continue;

            ensureAccountExists(followedAccount);

            Optional<GraphEdgeEntity> existing = graphEdgeRepository
                    .findByFromAccountAndToAccount(account.getAccountId(), followedAccount);
            if (existing.isEmpty()) {
                graphEdgeRepository.save(
                        GraphEdgeEntity.builder()
                                .fromAccount(account.getAccountId())
                                .toAccount(followedAccount)
                                .connectedAt(LocalDateTime.now())
                                .build()
                );
            }
        }
    }

    private void ensureAccountExists(String accountId) {
        if (accountRepository.findByAccountId(accountId).isEmpty()) {
            accountRepository.save(
                    AccountEntity.builder()
                            .accountId(accountId)
                            .createdAt(LocalDateTime.now())
                            .postCount(0)
                            .followerCount(0)
                            .avgIntervalSec(0.0)
                            .topicHistory("")
                            .behavioralScore(0.0)
                            .isActive(true)
                            .build()
            );
        }
    }

    private void updateTemporalBucket(PostRequest request) {
        String topic = request.getTopicBucket() != null ? request.getTopicBucket() : "general";
        LocalDateTime windowStart = request.getPostedAt()
                .withMinute(request.getPostedAt().getMinute() / 5 * 5)
                .withSecond(0)
                .withNano(0);

        TemporalBucketEntity bucket = temporalBucketRepository
                .findByTopicAndWindowStart(topic, windowStart)
                .orElseGet(() -> temporalBucketRepository.save(
                        TemporalBucketEntity.builder()
                                .topic(topic)
                                .windowStart(windowStart)
                                .postCount(0)
                                .suspiciousCount(0)
                                .build()
                ));

        bucket.setPostCount(bucket.getPostCount() + 1);
        temporalBucketRepository.save(bucket);
    }
}
