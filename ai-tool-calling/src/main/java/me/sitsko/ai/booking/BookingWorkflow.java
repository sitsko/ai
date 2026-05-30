package me.sitsko.ai.booking;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import io.smallrye.config.SmallRyeConfig;
import me.sitsko.ai.parser.ParserAgent;
import me.sitsko.ai.schedule.ScheduleAgent;
import me.sitsko.ai.schedule.VoyageData;
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


