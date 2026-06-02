package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@ApplicationScoped
public class TokenMetricCollector {

	@Getter
	private final Map<String, AtomicInteger> inputTokens = new ConcurrentHashMap<>();

	@Getter
	private final Map<String, AtomicInteger> outputTokens = new ConcurrentHashMap<>();

	public void onResponse(@Observes AiServiceResponseReceivedEvent event) {
		TokenUsage usage = event.response().tokenUsage();
		if (usage == null) return;

		String model = event.response().modelName();

		inputTokens.computeIfAbsent(model, m ->
						new AtomicInteger(usage.inputTokenCount()))
				.addAndGet(usage.inputTokenCount());

		outputTokens.computeIfAbsent(model, m ->
				new AtomicInteger(usage.outputTokenCount()))
				.addAndGet(usage.inputTokenCount());
	}

	public void clearTokenStatistics() {
		flushTokens(inputTokens);
		flushTokens(outputTokens);
	}

	private void flushTokens(Map<String, AtomicInteger> inputCounters) {
		for (AtomicInteger counter : inputCounters.values()) {
			counter.set(0);
		}
	}
}
