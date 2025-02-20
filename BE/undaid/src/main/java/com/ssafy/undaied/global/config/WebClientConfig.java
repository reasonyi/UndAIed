package com.ssafy.undaied.global.config;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {
    @Value("${ai.server.url}")
    private String aiHost;
    @Bean
    public WebClient webClient() {


        return WebClient.builder()
            .baseUrl(aiHost)
            // .baseUrl("http://localhost:8000")
            .build();
    }
}