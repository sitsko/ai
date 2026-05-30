package me.sitsko.ai.booking;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import me.sitsko.ai.schedule.VoyageData;

@Builder
public record BookingResponse(
		String userRequest,
		BookingStructuredRequest bookingStructuredRequest,
		VoyageData voyageData,
		String llmProvider,
		String error) {
}
