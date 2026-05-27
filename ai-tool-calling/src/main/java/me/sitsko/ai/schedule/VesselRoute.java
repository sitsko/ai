package me.sitsko.ai.schedule;

import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDateTime;

public record VesselRoute(
		@Description("Departure city")
		String fromCity,

		@Description("Arrival city")
		String toCity,

		@Description("Departure date")
		LocalDateTime departure,

		@Description("Arrival date")
		LocalDateTime arrival,

		@Description("Vessel name")
    String vessel
) {

}
