package com.yizhaoqi.gitintelligence.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhaoqi.gitintelligence.config.RerankProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HttpRerankClient implements RerankClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RerankProperties properties;

    public HttpRerankClient(ObjectMapper objectMapper, RerankProperties properties) {
        this.webClient = WebClient.builder().baseUrl(properties.getBaseUrl()).build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public List<RerankScore> score(String query, List<String> documents) {
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        request.put("documents", documents);
        request.put("model", properties.getModel());
        request.put("topN", documents.size());

        String response = webClient.post()
                .uri("/rerank")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block(properties.getTimeout());
        return parseResponse(response);
    }

    private List<RerankScore> parseResponse(String response) {
        try {
            JsonNode results = objectMapper.readTree(response).path("results");
            if (!results.isArray()) {
                throw new IllegalStateException("rerank response missing results array");
            }
            return java.util.stream.StreamSupport.stream(results.spliterator(), false)
                    .map(node -> new RerankScore(node.path("index").asInt(-1), node.path("score").asDouble()))
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("invalid rerank response", e);
        }
    }
}