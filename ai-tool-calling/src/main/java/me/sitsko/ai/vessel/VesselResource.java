package me.sitsko.ai.vessel;

import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Endpoints for vessel-related operations.
 *
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

	@GET
	@Path("/vessels/{owner}/count")
	public int countVesel(@PathParam("owner") String owner) {
		return vesselAiService.countVessels(owner);
	}

	@ServerExceptionMapper
	public RestResponse<ExceptionResponse> mapExceptionIn(InputGuardrailException ex) {
		log.warn("Input Guard rail detects prohibited request", ex);
		return ResponseBuilder.<ExceptionResponse>create(Status.FORBIDDEN)
				.entity(new ExceptionResponse("Ops"))
				.build();
	}
}


