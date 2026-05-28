package me.sitsko.ai.booking;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface BookingAgent {

	@SystemMessage("""
			You are a booking agent for containers in liner vessel.
			
			You should provide some solutions about suitable vessel's schedules to book containers.
			Return JSON only.
			
			Today is {current_date}.
			""")
	@SequenceAgent(
			subAgents = { UserParserAgent.class, VesselScheduleAgent.class}
	)
	BookingResponse reservationData(String userRequest);

	@Output
	static BookingResponse output(String userRequest, RequestedRoute requestedRoute , ResponseRoutes responseRoutes) {
		return new BookingResponse(userRequest, requestedRoute, responseRoutes, "");
	}
}


