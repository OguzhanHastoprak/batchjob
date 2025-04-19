package com.example.batchJob.Config;

import com.example.batchJob.model.ApiDataFetch;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ApiDataFetch> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ApiDataFetch> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        Jackson2JsonRedisSerializer<ApiDataFetch> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, ApiDataFetch.class);

        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Boolean> booleanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Boolean> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Boolean.class));
        return redisTemplate;
    }
}
