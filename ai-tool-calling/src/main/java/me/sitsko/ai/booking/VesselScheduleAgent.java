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
			
			You should provide most suitable routes from existing schedule according user preferences about  departure and arrival dates.
			
			Response has to contain from 1 up to 3 proposals, ordered by best relevance.
			Format output only JSON:
			
		  {
          "routes": [
              {
                  "vessel": "<vessel name>",
                  "route": "<from city to city>",
                  "trip date": "<departure - arrival date>"
              }
          ]
      }

      Example 1:
			User query:
			
			""")
	@UserMessage("""
      User request: {route}
      Today is {current_date}.
      """	)
	@ToolBox(VesselScheduler.class)
	@Agent(name = "MrFrontDesker",
			value = "Provides most suitable routes from existing schedule according user preferences about departure and arrival dates.",
	  outputKey = "scheduleResults")
	String reservationData(RequestedRoute route);
}

