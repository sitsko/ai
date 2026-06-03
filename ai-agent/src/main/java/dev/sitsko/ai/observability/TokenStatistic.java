package dev.sitsko.ai.observability;

import java.util.List;

public record TokenStatistic(
		List<TokenUsage> inputTokenStatistic,
		List<TokenUsage> outputTokenStatistic) {

}
