package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerTokenExporterTest {

    private SimpleMeterRegistry registry;
    private TokenMetricCollector collector;
    private MicrometerTokenExporter exporter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        collector = new TokenMetricCollector();
        exporter = new MicrometerTokenExporter(registry, collector);
    }

    @Test
    void record_registersInputAndOutputGaugesForNewKey() {
        exporter.record(new ModelAgentKey("gpt-4o-mini", "ParserAgent"), new TokenUsage(10, 5));

        Gauge inputGauge = registry.find("ai_agent.token.input")
                .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").gauge();
        Gauge outputGauge = registry.find("ai_agent.token.output")
                .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").gauge();

        assertThat(inputGauge).isNotNull();
        assertThat(outputGauge).isNotNull();
        assertThat(inputGauge.value()).isEqualTo(10.0);
        assertThat(outputGauge.value()).isEqualTo(5.0);
    }

    @Test
    void record_gaugeReflectsAccumulatedValue() {
        var key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");
        exporter.record(key, new TokenUsage(10, 5));
        exporter.record(key, new TokenUsage(3, 2));

        Gauge inputGauge = registry.find("ai_agent.token.input")
                .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").gauge();

        assertThat(inputGauge.value()).isEqualTo(13.0);
    }

    @Test
    void record_gaugeDropsToZeroAfterCollectorClear() {
        var key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");
        exporter.record(key, new TokenUsage(10, 5));

        collector.clearTokenStatistics();

        Gauge inputGauge = registry.find("ai_agent.token.input")
                .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").gauge();

        assertThat(inputGauge.value()).isZero();
    }

    @Test
    void record_registersDistinctGaugesPerKey() {
        exporter.record(new ModelAgentKey("gpt-4o-mini", "ParserAgent"), new TokenUsage(10, 5));
        exporter.record(new ModelAgentKey("claude-sonnet-4-6", "ScheduleAgent"), new TokenUsage(20, 8));

        assertThat(registry.find("ai_agent.token.input").tag("agent", "ParserAgent").gauge()).isNotNull();
        assertThat(registry.find("ai_agent.token.input").tag("agent", "ScheduleAgent").gauge()).isNotNull();
    }

    @Test
    void record_doesNotDuplicateGaugesOnRepeatedKey() {
        var key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");
        exporter.record(key, new TokenUsage(10, 5));
        exporter.record(key, new TokenUsage(3, 2));

        long inputGaugeCount = registry.find("ai_agent.token.input")
                .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent")
                .meters().size();
        assertThat(inputGaugeCount).isEqualTo(1);
    }
}
