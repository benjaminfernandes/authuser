package com.ead.authuser.configs;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private final int TIMEOUT = 5000;

    @LoadBalanced //REstTemplate tem suporte para balanceamento de cargas - se beneficia das instancias eurekas
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
    //Do any additional configuration here
        return builder
                .setConnectTimeout(Duration.ofMillis(TIMEOUT))
                .setReadTimeout(Duration.ofMillis(TIMEOUT))
                .build();
    }

}
