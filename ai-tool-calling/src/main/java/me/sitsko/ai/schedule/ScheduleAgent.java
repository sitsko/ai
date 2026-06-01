package me.sitsko.ai.schedule;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.ToolBox;
import me.sitsko.ai.booking.BookingStructuredRequest;
import me.sitsko.ai.security.OutputGuardrailService;

public interface ScheduleAgent {

	@SystemMessage("""
			You are a vessel schedule agent for containers in liner vessel company Hapag Lloyd.
			
			You should provide most suitable voyages from existing schedule according user preferences about  departureDate and arrivalDate dates, and containers count.
			
			Response has to contain from 1 up to 3 proposals, the departureDate and arrivalDate date deviation has not exceed 3 days.
			The proposals has to be ordered by best relevance, i.e. with lowest date deviation compare to requirements.
			
			Format output only JSON:
			
			{
				"voyages": [
							{
                  "vessel": "<vessel name>",
                  "route": "<departure port => arrival port>",
                  "trip": "<departure date => arrival date>",
                  "containerCount" : <number of containers>
              }
          ]
      }

			IMPORTANT: Your response MUST be a raw JSON object only.
			Do NOT wrap it in markdown code blocks (no ```json, no ```, no backticks).
			Do NOT add any explanation, prefix, or suffix.
			Your entire response must start with '{' and end with '}'.
			""")
	@UserMessage("""
      User request: {bookingStructuredRequest}
      Today is {current_date}.
      """	)
	@ToolBox(ScheduleService.class)
	@OutputGuardrails(OutputGuardrailService.class)
	@Agent(name = "MrScheduler",
			value = "Provides most suitable voyages from existing schedule according user preferences about departureDate and arrivalDate dates.",
	    outputKey = "voyageData")
	VoyageData adviceRoutes(BookingStructuredRequest bookingStructuredRequest);
}

