package com.narrativewatch.controller;

import com.narrativewatch.dto.PostRequest;
import com.narrativewatch.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/posts")
    public ResponseEntity<Void> ingestPost(@RequestBody PostRequest request) {
        ingestionService.ingestPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
