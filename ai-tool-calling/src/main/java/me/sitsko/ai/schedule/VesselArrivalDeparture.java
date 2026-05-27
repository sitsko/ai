package me.sitsko.ai.schedule;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record VesselArrivalDeparture(
		String vessel,
		LocalDate arrival,
		LocalDate departure
) {
 public static VesselArrivalDeparture of(String vessel, String departure, String arrival) {
	 return new VesselArrivalDeparture(vessel, LocalDate.parse(arrival), LocalDate.parse(departure));
 }
}
