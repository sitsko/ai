package me.sitsko.ai.schedule;

import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDate;

public record VesselRoute(
		@Description("Departure city")
		String fromCity,

		@Description("Arrival city")
		String toCity,

		@Description("Departure date")
		LocalDate departure,

		@Description("Arrival date")
		LocalDate arrival,

		@Description("Vessel name")
    String vessel
) {

}
