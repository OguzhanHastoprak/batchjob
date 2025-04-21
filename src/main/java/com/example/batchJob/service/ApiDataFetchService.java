package com.example.batchJob.service;

import com.example.batchJob.controller.ApiDataFetchController;
import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.repository.ApiDataFetchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiDataFetchService {
    private final ApiDataFetchRepository apiDataFetchRepository;

    public ApiDataFetchService(ApiDataFetchRepository apiDataFetchRepository) {
        this.apiDataFetchRepository = apiDataFetchRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(ApiDataFetchService.class);

    @Autowired
    private RedisTemplate<String, ApiDataFetch> redisTemplate;

    @Autowired
    private RedisTemplate<String, Boolean> booleanRedisTemplate;

    public Optional<ApiDataFetch> getLatestData() {
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
                    return Optional.of(latest);
                }
            }

            if (cached != null) {
                logger.info("Returning cached data: {}", cached);
                return Optional.of(cached);
            }

            logger.info("No data found");
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving data", e);
            return Optional.empty();
        }
    }
}
