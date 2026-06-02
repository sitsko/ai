package me.sitsko.ai.schedule;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import me.sitsko.ai.exception.VoyageDataSourceException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ScheduleService {

	@ConfigProperty(name = "app.voyages.path")
	String voyagesPath;

	@Tool("""
	Return schedules from the voyage database for vessels that can transport containers from {departurePort} to {arrivalPort}
	departing between {departureDateFrom} and {departureDateTo}.
	""")
	public List<Coastal> findRoute(
			String departurePort,
			String arrivalPort,
			LocalDate departureDateFrom,
			LocalDate departureDateTo) {
		List<Coastal> results = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(voyagesPath))) {
			reader.readLine(); // skip header
			String line;
			while ((line = reader.readLine()) != null) {
				parseLine(departurePort, arrivalPort, departureDateFrom, departureDateTo, line, results);
			}
		} catch (IOException e) {
			throw new VoyageDataSourceException("Failed to read voyages file: " + voyagesPath, e);
		}
		return results;
	}

	private void parseLine(
			String departurePort,
			String arrivalPort,
			LocalDate departureDateFrom,
			LocalDate departureDateTo,
			String line,
			List<Coastal> results) {
		String[] parts = line.split(";");
		if (parts.length < 5)
			return;
		String depPort = parts[0].trim();
		String arrPort = parts[1].trim();
		String vessel  = parts[2].trim();
		LocalDate depDate = LocalDate.parse(parts[3].trim());
		LocalDate arrDate = LocalDate.parse(parts[4].trim());
		if (depPort.equals(departurePort) && arrPort.equals(arrivalPort)
				&& depDate.isAfter(departureDateFrom) && depDate.isBefore(departureDateTo)) {
			results.add(new Coastal(departurePort, arrivalPort, depDate, arrDate, vessel));
		}
	}
}
