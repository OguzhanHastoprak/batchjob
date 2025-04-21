package com.example.batchJob.controller;

import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.repository.ApiDataFetchRepository;
import com.example.batchJob.service.ApiDataFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/data-fetch")
public class ApiDataFetchController {
    private final ApiDataFetchService apiDataFetchService;

    public ApiDataFetchController(ApiDataFetchService apiDataFetchService) {
        this.apiDataFetchService = apiDataFetchService;
    }
    /*
    private static final Logger logger = LoggerFactory.getLogger(ApiDataFetchController.class);

    @Autowired
    private RedisTemplate<String, ApiDataFetch> redisTemplate;

    @Autowired
    private RedisTemplate<String, Boolean> booleanRedisTemplate;

    @Autowired
    private ApiDataFetchRepository apiDataFetchRepository; */

    @GetMapping
    public ResponseEntity<ApiDataFetch> getLatestData() {
        return this.apiDataFetchService.getLatestData()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
