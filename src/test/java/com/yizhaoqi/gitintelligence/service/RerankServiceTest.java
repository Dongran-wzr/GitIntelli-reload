package com.yizhaoqi.gitintelligence.service;

import com.yizhaoqi.gitintelligence.client.RerankClient;
import com.yizhaoqi.gitintelligence.config.RerankProperties;
import com.yizhaoqi.gitintelligence.entity.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RerankServiceTest {

    private RerankClient client;
    private RerankService service;

    @BeforeEach
    void setUp() {
        client = mock(RerankClient.class);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(true);
        service = new RerankService(client, properties);
    }

    @Test
    void reordersCandidatesByRerankScoreAndLimitsTopK() {
        when(client.score(eq("query"), anyList())).thenReturn(List.of(
                new RerankClient.RerankScore(1, 0.9),
                new RerankClient.RerankScore(0, 0.4)
        ));

        List<SearchResult> ranked = service.rerank("query", candidates(), 1);

        assertEquals(2, ranked.get(0).getChunkId());
        assertEquals(0.9, ranked.get(0).getRerankScore());
        assertEquals(0.2, ranked.get(0).getRetrievalScore());
    }

    @Test
    void preservesRetrievalOrderWhenClientFails() {
        when(client.score(anyString(), anyList())).thenThrow(new RuntimeException("offline"));

        assertEquals(List.of(1, 2), service.rerank("query", candidates(), 2)
                .stream().map(SearchResult::getChunkId).toList());
    }

    private List<SearchResult> candidates() {
        return List.of(
                new SearchResult("a", 1, "first", 0.4),
                new SearchResult("b", 2, "second", 0.2)
        );
    }
}
