package com.example.circuitbreakerreading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@EnableCircuitBreaker // required for test t2
@SpringBootApplication
public class CircuitBreakerReadingApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CircuitBreakerReadingApplication.class, args);
    }

    // required for t1 test to run successfully
    @Configuration
    public class Config {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplateBuilder().build();
        }
    }
}
