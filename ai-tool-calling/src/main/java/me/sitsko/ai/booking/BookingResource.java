package me.sitsko.ai.booking;

import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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


	@GET()
	@Path("/proposal")
	public BookingResponse proposeVariants(BookingRequest bookingRequest) {
		log.info("user request: {}", bookingRequest.toString());
		return bookingAgent.reservationData(bookingRequest.userPrompt());
	}

	@ServerExceptionMapper
	public RestResponse<BookingResponse> mapException(InputGuardrailException ex) {
		log.warn("Security violation detected in booking request", ex);
		return ResponseBuilder.<BookingResponse>create(Status.FORBIDDEN)
				.entity(BookingResponse.builder()
						.advice("Detected Security Violation")
						.build())
				.build();
	}

}
