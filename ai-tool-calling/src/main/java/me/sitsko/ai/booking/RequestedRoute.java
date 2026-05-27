package me.sitsko.ai.booking;

import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDateTime;

public record RequestedRoute(
		@Description("Departure city")
		String fromCity,

		@Description("Arrival city")
		String toCity,

		@Description("Departure date")
		LocalDateTime departure,

		@Description("Arrival date")
		LocalDateTime arrival,

		@Description("Numbers of containers")
		int containerCount,

		@Description("How confident requirement about departure date")
		double departureConfidence,

		@Description("How confident requirement about arrival date")
		double arrivalConfidence
) {

}
