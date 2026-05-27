package me.sitsko.ai.booking;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;
import me.sitsko.ai.schedule.VesselScheduler;

@ApplicationScoped
@RegisterAiService
public interface VesselScheduleAgent {

	@SystemMessage("""
			You are a vessel schedule agent for containers in liner vessel company Hapag Lloyd.
			
			You should provide most suitable routes from existing schedule according user preferences about  departure and arrival dates, and containers count.
			
			Response has to contain from 1 up to 3 proposals, the departure and arrival date deviation has not exceed 3 days.
			The proposals has to be ordered by best relevance, i.e. with lowest date deviation compare to requirements.
			
			Format output only JSON:
			
			{
				"routes": [
							{
                  "vessel": "<vessel name>",
                  "route": "<from city => to city>",
                  "trip": "<departure => arrival date>",
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
      User request: {requestedRoute}
      Today is {current_date}.
      """	)
	@ToolBox(VesselScheduler.class)
	@Agent(name = "MrFrontDesker",
			value = "Provides most suitable routes from existing schedule according user preferences about departure and arrival dates.",
	    outputKey = "responseRoutes")
	ResponseRoutes adviceRoutes(RequestedRoute requestedRoute);
}

