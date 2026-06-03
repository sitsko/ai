package dev.sitsko.ai.schedule;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.ToolBox;
import dev.sitsko.ai.booking.BookingStructuredRequest;
import dev.sitsko.ai.security.OutputGuardrailService;

public interface ScheduleAgent {

	@SystemMessage("""
			You are a vessel schedule agent for container shipping at Hapag-Lloyd liner company.

			Steps you MUST follow:
			1. Call the schedule tool to query voyages for the requested departure port, arrival port,
			   and a departure date window of -/+5 days around the user's requested departure date.
			2. From the returned results, select up to 3 best-matching voyages.
			3. Rank proposals by departure date match relevance.
			4. Set containerCount in each proposal to the value from the user request.
			5. Calculate adviceScore using date-mismatch penalties:
				   - departure penalty = |voyage.departureDate - request.departureDate| (days) * departureConfidence * 0.1
				     (0 if request.departureDate is null)
				   - arrival penalty   = |voyage.arrivalDate - request.arrivalDate| (days) * arrivalConfidence * 0.05
				     (0 if request.arrivalDate is null)
				   - adviceScore = max(0.0, 1.0 - departure penalty - arrival penalty), rounded to 2 decimal places

			Respond with raw JSON only, using this exact structure:

			{
				"voyages": [
					{
						"vessel": "<vessel name>",
						"route": "<departure port> => <arrival port>",
						"trip": "<departure date yyyy-MM-dd> => <arrival date yyyy-MM-dd>",
						"containerCount": <number of containers>,
						"adviceScore": <score value from 0.0 to 1.0>
					}
				]
			}

			IMPORTANT: Your response MUST be a raw JSON object only.
			Do NOT wrap it in markdown code blocks (no ```json, no ```, no backticks).
			Do NOT add any explanation, prefix, or suffix.
			Your entire response must start with '{' and end with '}'.
			If no matching voyages are found, respond with: {"voyages": []}
			""")
	@UserMessage("""
      User request: {bookingStructuredRequest}
      Today is {current_date}.
      """	)
	@ToolBox(ScheduleService.class)
	@OutputGuardrails(OutputGuardrailService.class)
	@Agent(name = "Fröken Hildur Bock",
			value = "Provides most suitable voyages from existing schedule according user preferences about departureDate and arrivalDate dates.",
		    outputKey = "voyageData")
	VoyageData adviceRoutes(BookingStructuredRequest bookingStructuredRequest);
}
