package com.yizhaoqi.gitintelligence.client;

import java.util.List;

public interface RerankClient {
    List<RerankScore> score(String query, List<String> documents);

    record RerankScore(int index, double score) {
    }
}