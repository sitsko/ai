package dev.sitsko.ai.observability;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@Path("/api/metrics")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class MetricResource {

	private final TokenMetricCollector tokenMetricCollector;

	@Path("/tokens")
	@GET
	public TokenStatistic fetchTokenStatistics(){
		List<TokenUsage> inputTokens = tokenMetricCollector.getInputTokens().entrySet().stream()
				.map(e -> new TokenUsage(e.getKey(), e.getValue().get()))
				.toList();
		List<TokenUsage> outputTokens = tokenMetricCollector.getOutputTokens().entrySet().stream()
				.map(e -> new TokenUsage(e.getKey(), e.getValue().get()))
				.toList();
		return new TokenStatistic(inputTokens, outputTokens);
	}

	@Path("/tokens")
	@DELETE
	public void flushTokenStatistics(){
		tokenMetricCollector.clearTokenStatistics();
	}
}
