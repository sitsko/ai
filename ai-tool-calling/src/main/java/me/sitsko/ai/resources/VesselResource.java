package me.sitsko.ai.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.sitsko.ai.vessel.Vessel;
import me.sitsko.ai.vessel.VesselAiService;

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
	@Path("/vessels/heavy")
	public Vessel findHeavyVesel() {
		return vesselAiService.getHeavyVessel();
	}
}


