package me.sitsko.ai.booking;

import dev.langchain4j.agentic.agent.AgentInvocationException;
import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.OutputGuardrailException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.sitsko.ai.schedule.VoyageData;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RequiredArgsConstructor
@Path("/api/booking")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BookingResource {

	private final BookingWorkflow bookingWorkflow;


	@POST
	@Path("/proposal")
	public BookingResponse proposeVariants(BookingRequest bookingRequest) {
		log.info("user request: {}", bookingRequest.toString());
		return bookingWorkflow.reservationData(bookingRequest.userPrompt());
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapAgentInvocationException(AgentInvocationException ex) {
		Throwable cause = ex.getCause();
		while (cause != null) {
			switch (cause) {
				case InputGuardrailException guardrailEx -> { return mapExceptionIn(guardrailEx); }
				case OutputGuardrailException guardrailEx -> { return mapExceptionOut(guardrailEx); }
				default -> cause = cause.getCause();
			}
		}
		log.error("Unhandled agent invocation error", ex);
		return ResponseBuilder.<BookingResponse>create(Status.INTERNAL_SERVER_ERROR)
				.entity(BookingResponse.builder()
						.voyageData(new VoyageData(Collections.emptyList()))
						.error(ex.getMessage())
						.build())
				.build();
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapExceptionIn(InputGuardrailException ex) {
		log.warn("Security violation detected in booking request", ex);
		return ResponseBuilder.<BookingResponse>create(Status.FORBIDDEN)
				.entity(BookingResponse.builder()
						.voyageData(new VoyageData(Collections.emptyList()))
						.error(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
						.build())
				.build();
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapExceptionOut(OutputGuardrailException ex) {
		log.warn("Security violation detected in booking response", ex);
		return ResponseBuilder.<BookingResponse>create(Status.FORBIDDEN)
				.entity(BookingResponse.builder()
						.voyageData(new VoyageData(Collections.emptyList()))
						.error(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
						.build())
				.build();
	}
}
