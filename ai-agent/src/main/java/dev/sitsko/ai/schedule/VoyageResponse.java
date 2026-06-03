package dev.sitsko.ai.schedule;

public record VoyageResponse(
		String vessel,
		String route,
		String trip,
		int containerCount,
		double adviceScore
) {

}
