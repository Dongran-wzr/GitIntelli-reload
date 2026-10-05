package com.yizhaoqi.gitintelligence.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "rerank")
public class RerankProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:8082";
    private String model = "BAAI/bge-reranker-base";
    private int candidates = 20;
    private Duration timeout = Duration.ofSeconds(5);
    private int maxTextLength = 1500;
}