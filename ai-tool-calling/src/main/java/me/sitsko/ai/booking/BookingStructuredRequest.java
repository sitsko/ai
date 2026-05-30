package me.sitsko.ai.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDate;

public record BookingStructuredRequest(
		@Description("Departure city")
		String fromCity,

		@Description("Arrival city")
		String toCity,

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		@Description("Departure date")
		LocalDate departure,

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		@Description("Arrival date")
		LocalDate arrival,

		@Description("Numbers of containers")
		int containerCount,

		@Description("How confident requirement about departure date")
		double departureConfidence,

		@Description("How confident requirement about arrival date")
		double arrivalConfidence
) {

}
