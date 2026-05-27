package me.sitsko.ai.booking;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;
import me.sitsko.ai.schedule.VesselScheduler;

@ApplicationScoped
@RegisterAiService
public interface UserParserAgent {

	@SystemMessage("""
			You are a front desk agent which has to understand user requirements for booking container and create structured output.
			
			You should extract from user request such information as
			from which city containers should be picked up, to which city they should be delivered, and preferable departure and arrival date, and how many containers.
			Confidence about departure date has to be in range from 0.0 to 1.0, where 1.0 means that a user are sure about the date, and 0.0 means that a user are not sure at all.
			Confidence about arrival date has to be in range from 0.0 to 1.0, where 1.0 means that a user are sure about the date, and 0.0 means that a user are not sure at all.
			If a user does not provide any information about departure date, you can use (current date + 3 day) as default value with confidence 0.2.
			If a user does not provide any information about arrival date, you can use null value with confidence 0.0.
			if a user do not mention year, assume that it is current year.
			
			Output structure MUST be a JSON only, no any additional strings and words:
			============= JSON structure begins
			{
			   "fromCity" : <Departure City Name>,
			   "toCity" : <Arrival City Name>,
			   "departure" : <Departure Date in ISO format without timezone>,
			   "arrival" : <Arrival Date in ISO format without timezone>,
				 "containerCount" : <number of containers to be reserved>,
	       "departureConfidence" : <confidence in departure date, double number between 0.0 and 1.0>,
         "arrivalConfidence" : <confidence in arrival date, double number between 0.0 and 1.0>
      }
      JSON structure ended =================
			
			The current date for examples is 2026-09-01.
			Example 1.
			User query: I need a reservation 5 containers from Hamburg to Gdansk. The containers has to be arrived on 25 September
			Output:
			{
			   "fromCity" : "Hamburg",
			   "toCity" : "Gdansk",
			   "departure" : "2026-09-03", 
			   "arrival" : "2026-09-25",
				 "containerCount" : 5,
	       "departureConfidence" : 0.2,
				 "arrivalConfidence" : 0.9
			} 
			
			Example 2.
			User query: I need a reservation 100 containers from Hamburg to Barcelona. The container will be load approximetly on  15th september, The containers must be arrived on 20 September not later
			Output:
			{
			   "fromCity" : "Hamburg",
			   "toCity" : "Barcelona", 
			   "departure" : "2026-09-15", 
			   "arrival" : "2026-09-25",
				 "containerCount" : 100,
	       "departureConfidence" : 0.6,
				 "arrivalConfidence" : 1.0
			} 
			
			Example 3.
			User query: I want to deliver 10 containers from Gdansk to Tokio.  It is quite urgent so we need to send them before 08.09.2026
			Output:
			{
			   "fromCity" : "Gdansk",
			   "toCity" : "Tokio", 
			   "departure" : "2026-09-08", 
			   "arrival" : null,
				 "containerCount" : 10,
	       "departureConfidence" : 0.9,
				 "arrivalConfidence" : 0.0
			} 
			""")
	@UserMessage("""
			Parse user request and extract data in JSON format
			User query: {userRequest}
			Current date: {current_date}
			""")
	RequestedRoute reservationData(String userRequest);
}

