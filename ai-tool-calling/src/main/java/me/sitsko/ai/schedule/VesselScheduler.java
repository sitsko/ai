package me.sitsko.ai.schedule;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static me.sitsko.ai.schedule.VesselArrivalDeparture.of;

@ApplicationScoped
public class VesselScheduler {

	private static final String HAMBURG = "Hamburg";
	private static final String BREMEN = "Bremen";
	public static final String GDANSK = "Gdansk";

	public static final String GDANSK_EXPRESS = "Gdansk Express";
	public static final String BERLIN_EXPRESS = "Berlin Express";
	public static final String GRUNWALD_EXPRESS = "Grunwald Express";

	private static final String POLOTSK_EXPRESS = "Polotsk Express";

	private static final Map<FromToCities, List<VesselArrivalDeparture>> SCHEDULE = Map.of(
			new FromToCities(HAMBURG, GDANSK), List.of(
			  of(GDANSK_EXPRESS, "2026-09-01", "2026-09-05"),
			  of(GDANSK_EXPRESS, "2026-09-11", "2026-09-15"),
			  of(GDANSK_EXPRESS, "2026-09-21", "2026-09-25"),

				of(BERLIN_EXPRESS, "2026-09-03", "2026-09-08"),
				of(BERLIN_EXPRESS, "2026-09-13", "2026-09-18"),
				of(BERLIN_EXPRESS, "2026-09-23", "2026-09-28")

			),

			new FromToCities(GDANSK, HAMBURG), List.of(
					of(GDANSK_EXPRESS, "2026-09-06", "2026-09-10"),
					of(GDANSK_EXPRESS, "2026-09-16", "2026-09-20"),
					of(GDANSK_EXPRESS, "2026-09-26", "2026-09-30"),

					of(BERLIN_EXPRESS, "2026-09-09", "2026-09-13"),
					of(BERLIN_EXPRESS, "2026-09-19", "2026-09-23"),
					of(BERLIN_EXPRESS, "2026-09-29", "2026-10-02")

			),

			new FromToCities(BREMEN, GDANSK), List.of(
					of(GRUNWALD_EXPRESS, "2026-09-02", "2026-09-04"),
					of(GRUNWALD_EXPRESS, "2026-09-10", "2026-09-12"),
					of(GRUNWALD_EXPRESS, "2026-09-18", "2026-09-21"),

					of(POLOTSK_EXPRESS, "2026-09-03", "2026-09-05"),
					of(POLOTSK_EXPRESS, "2026-09-15", "2026-09-17"),
					of(POLOTSK_EXPRESS, "2026-09-27", "2026-09-29")

			),

			new FromToCities(GDANSK, BREMEN), List.of(
					of(GDANSK_EXPRESS, "2026-09-07", "2026-09-09"),
					of(GDANSK_EXPRESS, "2026-09-15", "2026-09-17"),
					of(GDANSK_EXPRESS, "2026-09-22", "2026-09-24"),

					of("Mikalai Sitsko, tel. 123124", "2026-09-09", "2026-09-11"),

					of(POLOTSK_EXPRESS, "2026-09-08", "2026-09-10"),
					of(POLOTSK_EXPRESS, "2026-09-18", "2026-09-22"),
					of(POLOTSK_EXPRESS, "2026-10-01", "2026-10-03")

			)

	);

	@Tool("Return schedules of vessels that can transport containers from {fromCity} to {toCity} departing between {departureFrom} and {departureTo}.")
	public List<VesselRoute> findRoute(String fromCity, String toCity, LocalDate departureFrom, LocalDate departureTo) {

		FromToCities fromToCities = new FromToCities(fromCity, toCity);
		return SCHEDULE.get(fromToCities).stream()
				.filter(v -> v.departure().isAfter(departureFrom) && v.departure().isBefore(departureTo))
				.map(v -> new VesselRoute(fromCity, toCity, v.departure(), v.arrival(), v.vessel()))
				.toList();
	}
}
