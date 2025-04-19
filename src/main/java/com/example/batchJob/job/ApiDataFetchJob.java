package com.example.batchJob.job;


import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.repository.ApiDataFetchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.city}")
    private String city;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            String url = String.format("%s?key=%s&q=%s", apiUrl, apiKey, city);
            String response = restTemplate.getForObject(url, String.class);
            logger.info("Fetched weather API response: {}", response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            String location = root.path("location").path("name").asText();
            double temperatureC = root.path("current").path("temp_c").asDouble();
            String condition = root.path("current").path("condition").path("text").asText();

            Optional<ApiDataFetch> latest = apiDataFetchRepository.findTopByOrderByFetchedAtDesc();
            boolean isNew = latest.map(data ->
                    !(data.getLocation().equals(location) &&
                            data.getTemperatureC() == temperatureC &&
                            data.getCondition().equals(condition))
            ).orElse(true);

            if (isNew) {
                ApiDataFetch apiDataFetch = new ApiDataFetch();
                apiDataFetch.setLocation(location);
                apiDataFetch.setTemperatureC(temperatureC);
                apiDataFetch.setCondition(condition);
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
