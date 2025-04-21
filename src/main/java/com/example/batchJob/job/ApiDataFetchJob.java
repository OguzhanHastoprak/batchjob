package com.example.batchJob.job;


import com.example.batchJob.Errors.WeatherApiException;
import com.example.batchJob.model.ApiDataFetch;
import com.example.batchJob.model.WeatherData;
import com.example.batchJob.repository.ApiDataFetchRepository;
import com.example.batchJob.service.WeatherApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.WeakKeyException;
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

    private final ApiDataFetchRepository apiDataFetchRepository;
    private final RedisTemplate<String, Boolean> booleanRedisTemplate;
    private final WeatherApiService weatherApiService;

    public ApiDataFetchJob(ApiDataFetchRepository apiDataFetchRepository,
                           RedisTemplate<String, Boolean> booleanRedisTemplate,
                           WeatherApiService weatherApiService){
        this.apiDataFetchRepository = apiDataFetchRepository;
        this.booleanRedisTemplate = booleanRedisTemplate;
        this.weatherApiService = weatherApiService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        try {
            WeatherData weatherData = weatherApiService.getWeatherData();
            processWeatherData(weatherData);
        } catch (WeatherApiException e) {
            logger.error("Error fetching weather data", e);
        } catch (Exception e) {
            logger.error("Error processing weather data", e);
        }
    }

    private void processWeatherData(WeatherData weatherData){
        if(isNewWeatherData(weatherData)){
            saveWeatherData(weatherData);
            updateNewDataFlag();
        } else {
            logger.info("No new data");
        }
    }

    private boolean isNewWeatherData(WeatherData weatherData){
        Optional<ApiDataFetch> latestFromDb = apiDataFetchRepository.findTopByOrderByFetchedAtDesc();
        return latestFromDb.map(data ->
                !(data.getLocation().equals(weatherData.location()) && // bir sebeple şehir değişirse
                        data.getTemperatureC() == weatherData.temperatureC() &&
                        data.getCondition().equals(weatherData.condition()))
        ).orElse(true);
    }

    private void saveWeatherData(WeatherData weatherData){
        ApiDataFetch apiDataFetch = new ApiDataFetch();
        apiDataFetch.setLocation(weatherData.location());
        apiDataFetch.setTemperatureC(weatherData.temperatureC());
        apiDataFetch.setCondition(weatherData.condition());
        apiDataFetch.setFetchedAt(LocalDateTime.now());
        apiDataFetchRepository.save(apiDataFetch);
        logger.info("New data saved to DB: {}", apiDataFetch);
    }

    private void updateNewDataFlag(){
        booleanRedisTemplate.opsForValue().set("hasNewData", true);
        logger.info("New data flag updated in Redis: {}", booleanRedisTemplate.opsForValue().get("hasNewData"));
    }
}
