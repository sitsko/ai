package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkiverse.langchain4j.observability.AiServiceSelector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class MicrometerTokenExporter {

    private final MeterRegistry registry;
    private final TokenMetricCollector collector;
    private final Set<ModelAgentKey> registeredKeys = ConcurrentHashMap.newKeySet();

    public void onParserResponse(@Observes @AiServiceSelector(dev.sitsko.ai.parser.ParserAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("ParserAgent", event);
    }

    public void onScheduleResponse(@Observes @AiServiceSelector(dev.sitsko.ai.schedule.ScheduleAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("ScheduleAgent", event);
    }

    public void onSecurityResponse(@Observes @AiServiceSelector(dev.sitsko.ai.security.SecurityAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("SecurityAgent", event);
    }

    public void onDataLeakResponse(@Observes @AiServiceSelector(dev.sitsko.ai.security.DataLeakAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("DataLeakAgent", event);
    }

    void record(ModelAgentKey key, TokenUsage usage) {
        collector.record(key, usage);
        if (registeredKeys.add(key)) {
            registerGauges(key);
        }
    }

    private void handleEvent(String agentName, AiServiceResponseReceivedEvent event) {
        TokenUsage usage = event.response().tokenUsage();
        if (usage == null) return;
        String model = event.response().modelName();
        record(new ModelAgentKey(model, agentName), usage);
    }

    private void registerGauges(ModelAgentKey key) {
        Gauge.builder("ai_agent.token.input", collector.getInputTokens(),
                        map -> map.getOrDefault(key, new AtomicInteger(0)).doubleValue())
                .tag("model", key.model())
                .tag("agent", key.agent())
                .register(registry);

        Gauge.builder("ai_agent.token.output", collector.getOutputTokens(),
                        map -> map.getOrDefault(key, new AtomicInteger(0)).doubleValue())
                .tag("model", key.model())
                .tag("agent", key.agent())
                .register(registry);
    }
}
