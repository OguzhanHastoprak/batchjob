package com.example.batchJob.controller;

import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.repository.ApiDataFetchRepository;
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

    private static final Logger logger = LoggerFactory.getLogger(ApiDataFetchController.class);

    @Autowired
    private RedisTemplate<String, ApiDataFetch> redisTemplate;

    @Autowired
    private RedisTemplate<String, Boolean> booleanRedisTemplate;

    @Autowired
    private ApiDataFetchRepository apiDataFetchRepository;

    @GetMapping
    public ResponseEntity<ApiDataFetch> getLatestData() {
        try {
            Boolean hasNewData = booleanRedisTemplate.opsForValue().get("hasNewData");
            logger.info("hasNewData flag from Redis: {}", hasNewData);

            ApiDataFetch cached = redisTemplate.opsForValue().get("latestData");
            if (Boolean.TRUE.equals(hasNewData) || cached == null) {
                Optional<ApiDataFetch> latestFromDb = apiDataFetchRepository.findTopByOrderByFetchedAtDesc();
                if (latestFromDb.isPresent()) {
                    ApiDataFetch latest = latestFromDb.get();
                    redisTemplate.opsForValue().set("latestData", latest);
                    booleanRedisTemplate.opsForValue().set("hasNewData", false);
                    logger.info("Cache updated and new data served from DB: {}", latest);
                    return ResponseEntity.ok(latest);
                }
            }

            if (cached != null) {
                logger.info("Returning cached data: {}", cached);
                return ResponseEntity.ok(cached);
            }

            logger.info("No data found");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error retrieving data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
