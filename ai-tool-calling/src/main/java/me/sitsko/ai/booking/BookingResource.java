package me.sitsko.ai.booking;

import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.OutputGuardrailException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	private final BookingAgent bookingAgent;


	@POST
	@Path("/proposal")
	public BookingResponse proposeVariants(BookingRequest bookingRequest) {
		log.info("user request: {}", bookingRequest.toString());
		return bookingAgent.reservationData(bookingRequest.userPrompt());
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapExceptionIn(InputGuardrailException ex) {
		log.warn("Security violation detected in booking request", ex);
		return ResponseBuilder.<BookingResponse>create(Status.FORBIDDEN)
				.entity(BookingResponse.builder()
						.responseRoutes(new ResponseRoutes(Collections.emptyList()))
						.error(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
						.build())
				.build();
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapExceptionOut(OutputGuardrailException ex) {
		log.warn("Security violation detected in booking response", ex);
		return ResponseBuilder.<BookingResponse>create(Status.FORBIDDEN)
				.entity(BookingResponse.builder()
						.responseRoutes(new ResponseRoutes(Collections.emptyList()))
						.error(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
						.build())
				.build();
	}
}
