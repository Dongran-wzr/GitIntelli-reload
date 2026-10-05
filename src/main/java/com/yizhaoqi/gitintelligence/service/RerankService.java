package com.yizhaoqi.gitintelligence.service;

import com.yizhaoqi.gitintelligence.client.RerankClient;
import com.yizhaoqi.gitintelligence.config.RerankProperties;
import com.yizhaoqi.gitintelligence.entity.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RerankService {
    private static final Logger logger = LoggerFactory.getLogger(RerankService.class);

    private final RerankClient client;
    private final RerankProperties properties;

    public RerankService(RerankClient client, RerankProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public List<SearchResult> rerank(String query, List<SearchResult> candidates, int topK) {
        if (candidates == null || candidates.isEmpty() || topK <= 0) {
            return List.of();
        }
        int limit = Math.min(topK, candidates.size());
        List<SearchResult> fallback = new ArrayList<>(candidates.subList(0, limit));
        if (!properties.isEnabled()) {
            return fallback;
        }

        try {
            List<String> documents = candidates.stream()
                    .map(SearchResult::getTextContent)
                    .map(text -> truncate(text, properties.getMaxTextLength()))
                    .toList();
            List<RerankClient.RerankScore> scores = client.score(query, documents);
            if (scores == null || scores.isEmpty()) {
                return fallback;
            }

            if (scores.size() != candidates.size()) {
                return fallback;
            }
            boolean[] seen = new boolean[candidates.size()];
            List<SearchResult> ranked = new ArrayList<>();
            for (RerankClient.RerankScore score : scores) {
                if (score.index() < 0 || score.index() >= candidates.size() || seen[score.index()]) {
                    return fallback;
                }
                seen[score.index()] = true;
                SearchResult result = candidates.get(score.index());
                result.setRetrievalScore(result.getScore());
                result.setRerankScore(score.score());
                result.setScore(score.score());
                ranked.add(result);
            }
            ranked.sort(Comparator.comparing(SearchResult::getRerankScore,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            return ranked.subList(0, Math.min(topK, ranked.size()));
        } catch (Exception e) {
            logger.warn("Rerank unavailable; returning retrieval order: {}", e.getMessage());
            return fallback;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || maxLength <= 0 || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength);
    }
}