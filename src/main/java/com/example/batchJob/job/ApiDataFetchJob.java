package com.example.batchJob.job;


import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.repository.ApiDataFetchRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ApiDataFetchJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ApiDataFetchJob.class);

    @Autowired
    private ApiDataFetchRepository apiDataFetchRepository;

    @Autowired
    private RedisTemplate<String, Boolean> booleanRedisTemplate;

    //@Autowired
    //private RedisTemplate<String, ApiDataFetch> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            String response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/todos/4", String.class);
            logger.info("Fetched API response: {}", response);

            Optional<ApiDataFetch> latest = apiDataFetchRepository.findTopByOrderByFetchedAtDesc();
            boolean isNew = latest.map(apiDataFetch -> !apiDataFetch.getPayload().equals(response)).orElse(true);
            if (isNew) {
                ApiDataFetch apiDataFetch = new ApiDataFetch();
                apiDataFetch.setPayload(response);
                apiDataFetch.setFetchedAt(LocalDateTime.now());
                apiDataFetchRepository.save(apiDataFetch);

                //redisTemplate.opsForValue().set("latestData", apiDataFetch);
                booleanRedisTemplate.opsForValue().set("hasNewData", true);

                logger.info("Saved new data to DB: {}", apiDataFetch);
            } else {
                logger.info("No new data fetched");
            }
        } catch (RestClientException e) {
            logger.error("Error fetching data from API", e);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
        }
    }
}
