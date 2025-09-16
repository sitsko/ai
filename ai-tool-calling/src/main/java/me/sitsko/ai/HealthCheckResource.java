package me.sitsko.ai;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@Path("/health")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class HealthCheckResource {

	@GET
	public String healthCheck() {
		log.info("Health check called.");
		return "{\"status\":\"ready\"}";
	}

}
