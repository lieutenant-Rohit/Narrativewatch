package com.narrativewatch.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostRequest {
    private String accountId;
    private String text;
    private LocalDateTime postedAt;
    private String topicBucket;
    private List<String> follows;
}
