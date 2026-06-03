package dev.sitsko.ai.booking;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import io.smallrye.config.SmallRyeConfig;
import dev.sitsko.ai.parser.ParserAgent;
import dev.sitsko.ai.schedule.ScheduleAgent;
import dev.sitsko.ai.schedule.VoyageData;
import org.eclipse.microprofile.config.ConfigProvider;

public interface BookingWorkflow {

	@SequenceAgent(
			subAgents = { ParserAgent.class, ScheduleAgent.class}
	)
	BookingResponse reservationData(String userRequest);

	@Output
	static BookingResponse output(String userRequest, BookingStructuredRequest bookingStructuredRequest, VoyageData voyageData) {
		SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);

		String llmProvider = config.getValue("quarkus.langchain4j.chat-model.provider", String.class);
		return new BookingResponse(userRequest, bookingStructuredRequest, voyageData, llmProvider, "");
	}
}
