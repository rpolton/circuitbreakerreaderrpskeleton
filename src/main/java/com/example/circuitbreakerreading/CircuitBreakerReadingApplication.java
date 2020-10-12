package com.example.circuitbreakerreading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@EnableCircuitBreaker
@SpringBootApplication
public class CircuitBreakerReadingApplication {

    @Configuration
    class Config {
        @Bean
        public RestTemplate rest(final RestTemplateBuilder builder) {
            return builder.build();
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(CircuitBreakerReadingApplication.class, args);
    }
}