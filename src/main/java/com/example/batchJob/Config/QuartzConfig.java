package com.example.batchJob.Config;

import com.example.batchJob.job.ApiDataFetchJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail apiDataFetchJobDetail() {
        return JobBuilder.newJob(ApiDataFetchJob.class)
                .withIdentity("apiDataFetchJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger apiDataFetchJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInHours(1) // Run every hour
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(apiDataFetchJobDetail())
                .withIdentity("apiDataFetchTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
