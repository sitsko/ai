package me.sitsko.ai.booking;

import lombok.Builder;

@Builder
public record BookingResponse(

		String userRequest,
		RequestedRoute requestedRoute,
		ResponseRoutes responseRoutes,
		String error) {
}
