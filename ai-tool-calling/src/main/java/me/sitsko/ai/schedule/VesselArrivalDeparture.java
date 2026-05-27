package me.sitsko.ai.schedule;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record VesselArrivalDeparture(
		String vessel,
		LocalDateTime arrival,
		LocalDateTime departure
) {
 public static VesselArrivalDeparture of(String vessel, String departure, String arrival) {
	 return new VesselArrivalDeparture(vessel, LocalDateTime.parse(arrival), LocalDateTime.parse(departure));
 }
}
