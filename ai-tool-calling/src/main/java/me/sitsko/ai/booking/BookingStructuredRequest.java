package me.sitsko.ai.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.langchain4j.model.output.structured.Description;
import java.time.LocalDate;

public record BookingStructuredRequest(
		@Description("Departure port")
		String departurePort,

		@Description("Arrival port")
		String arrivalPort,

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		@Description("Departure date")
		LocalDate departureDate,

		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		@Description("Arrival date")
		LocalDate arrivalDate,

		@Description("Numbers of containers")
		int containerCount,

		@Description("How confident requirement about departureDate date")
		double departureConfidence,

		@Description("How confident requirement about arrivalDate date")
		double arrivalConfidence
) {

}
