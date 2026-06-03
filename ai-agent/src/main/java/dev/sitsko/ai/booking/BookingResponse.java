package dev.sitsko.ai.booking;

import lombok.Builder;
import dev.sitsko.ai.schedule.VoyageData;

@Builder
public record BookingResponse(
		String userRequest,
		BookingStructuredRequest bookingStructuredRequest,
		VoyageData voyageData,
		String llmProvider,
		String error) {
}
