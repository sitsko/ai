package me.sitsko.ai.booking;

import lombok.Builder;
import me.sitsko.ai.schedule.VoyageData;

@Builder
public record BookingResponse(

		String userRequest,
		UserStructuredRequest userStructuredRequest,
		VoyageData voyageData,
		String error) {
}
