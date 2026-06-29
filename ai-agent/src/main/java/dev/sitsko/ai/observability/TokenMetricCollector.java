package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@ApplicationScoped
public class TokenMetricCollector {

    @Getter
    private final Map<ModelAgentKey, AtomicInteger> inputTokens = new ConcurrentHashMap<>();

    @Getter
    private final Map<ModelAgentKey, AtomicInteger> outputTokens = new ConcurrentHashMap<>();

    public void record(ModelAgentKey key, TokenUsage usage) {
        if (usage == null) return;

        inputTokens.computeIfAbsent(key, k -> new AtomicInteger(0))
                .addAndGet(usage.inputTokenCount());

        outputTokens.computeIfAbsent(key, k -> new AtomicInteger(0))
                .addAndGet(usage.outputTokenCount());
    }

    public void clearTokenStatistics() {
        flushTokens(inputTokens);
        flushTokens(outputTokens);
    }

    private void flushTokens(Map<ModelAgentKey, AtomicInteger> counters) {
        for (AtomicInteger counter : counters.values()) {
            counter.set(0);
        }
    }
}
