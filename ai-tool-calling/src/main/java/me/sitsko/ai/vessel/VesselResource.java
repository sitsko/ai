package me.sitsko.ai.vessel;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Mikalai Sitsko , 06/27/2025
 */
@Slf4j
@RequiredArgsConstructor
@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class VesselResource {

	private final VesselAiService vesselAiService;

	@GET
	@Path("/vessels/{owner}/heavy")
	public Vessel findHeavyVesel(@PathParam("owner") String owner) {
		return vesselAiService.getHeavyVessel(owner);
	}

	@GET
	@Path("/vessels/forecast")
	public String findHeavyVesel(@QueryParam("years") int years) {
		return vesselAiService.generateForecast(years);
	}
}


