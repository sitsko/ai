package dev.sitsko.ai.schedule;

import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDate;

public record Coastal(
		@Description("Departure port")
		String departurePort,

		@Description("Arrival port")
		String arrivalPort,

		@Description("Departure date")
		LocalDate departureDate,

		@Description("Arrival date")
		LocalDate arrivalDate,

		@Description("Vessel name")
    String vessel
) {

}
