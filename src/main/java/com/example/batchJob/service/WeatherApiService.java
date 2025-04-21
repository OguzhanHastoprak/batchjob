package com.example.batchJob.service;

import com.example.batchJob.Errors.WeatherApiException;
import com.example.batchJob.model.WeatherData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherApiService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;
    private final String city;

    public WeatherApiService(RestTemplate restTemplate,
                             @Value("${weather.api.url}") String apiUrl,
                             @Value("${weather.api.key}") String apiKey,
                             @Value("${weather.api.city}") String city) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.city = city;
    }

    public WeatherData getWeatherData() {
        String url = String.format("%s?key=%s&q=%s", apiUrl, apiKey, city);
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new WeatherApiException("Null response from weather API");
        }

        try {
            logger.debug("Received API response: {}", response);
            return parseWeatherData(response);
        } catch (Exception e) {
            logger.error("Error parsing weather data", e);
            throw new RuntimeException("Error parsing weather data", e);
        }
    }

    private WeatherData parseWeatherData(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            return new WeatherData(
                    rootNode.path("location").path("name").asText(),
                    rootNode.path("current").path("temp_c").asDouble(),
                    rootNode.path("current").path("condition").path("text").asText()
            );
        } catch (Exception e) {
            logger.error("Error parsing weather data", e);
            throw new WeatherApiException("Error parsing weather data", e);
        }
    }
}
