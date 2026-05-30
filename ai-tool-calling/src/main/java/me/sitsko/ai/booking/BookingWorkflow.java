package me.sitsko.ai.booking;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import me.sitsko.ai.parser.ParserAgent;
import me.sitsko.ai.schedule.ScheduleAgent;
import me.sitsko.ai.schedule.VoyageData;

public interface BookingWorkflow {

	@SequenceAgent(
			subAgents = { ParserAgent.class, ScheduleAgent.class}
	)
	BookingResponse reservationData(String userRequest);

	@Output
	static BookingResponse output(String userRequest, BookingStructuredRequest bookingStructuredRequest, VoyageData voyageData) {
		return new BookingResponse(userRequest, bookingStructuredRequest, voyageData, "");
	}
}


