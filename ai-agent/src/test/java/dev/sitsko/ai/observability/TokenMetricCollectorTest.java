package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenMetricCollectorTest {

    private final TokenMetricCollector collector = new TokenMetricCollector();
    private final ModelAgentKey key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");

    @Test
    void record_incrementsInputAndOutputTokens() {
        collector.record(key, new TokenUsage(10, 5));

        assertThat(collector.getInputTokens().get(key).get()).isEqualTo(10);
        assertThat(collector.getOutputTokens().get(key).get()).isEqualTo(5);
    }

    @Test
    void record_accumulatesAcrossMultipleCalls() {
        collector.record(key, new TokenUsage(10, 5));
        collector.record(key, new TokenUsage(3, 2));

        assertThat(collector.getInputTokens().get(key).get()).isEqualTo(13);
        assertThat(collector.getOutputTokens().get(key).get()).isEqualTo(7);
    }

    @Test
    void record_handlesNullUsageGracefully() {
        collector.record(key, null);

        assertThat(collector.getInputTokens()).doesNotContainKey(key);
        assertThat(collector.getOutputTokens()).doesNotContainKey(key);
    }

    @Test
    void clearTokenStatistics_resetsAllCountersToZero() {
        collector.record(key, new TokenUsage(10, 5));
        var key2 = new ModelAgentKey("claude-sonnet-4-6", "ScheduleAgent");
        collector.record(key2, new TokenUsage(20, 8));

        collector.clearTokenStatistics();

        assertThat(collector.getInputTokens().get(key).get()).isZero();
        assertThat(collector.getOutputTokens().get(key).get()).isZero();
        assertThat(collector.getInputTokens().get(key2).get()).isZero();
        assertThat(collector.getOutputTokens().get(key2).get()).isZero();
    }
}
