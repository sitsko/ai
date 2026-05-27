package me.sitsko.ai.booking;

import dev.langchain4j.model.output.structured.Description;
import lombok.Builder;

@Builder
public record BookingResponse(
		@Description("Information about possible routes in JSON format")
		String advice,

		String error) {

}
