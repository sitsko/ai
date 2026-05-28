package me.sitsko.ai.booking;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;

public interface BookingWorkflow {

	@SequenceAgent(
			subAgents = { UserParserAgent.class, VesselScheduleAgent.class}
	)
	BookingResponse reservationData(String userRequest);

	@Output
	static BookingResponse output(String userRequest, RequestedRoute requestedRoute , ResponseRoutes responseRoutes) {
		return new BookingResponse(userRequest, requestedRoute, responseRoutes, "");
	}
}


