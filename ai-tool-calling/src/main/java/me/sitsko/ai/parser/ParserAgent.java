package me.sitsko.ai.parser;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import me.sitsko.ai.booking.BookingStructuredRequest;
import me.sitsko.ai.security.InputGuardRailService;

public interface ParserAgent {

	@SystemMessage("""
			You are a front desk agent which has to understand user requirements for booking container and create structured output.
			
			You should extract from user request such information as
			from which port containers should be picked up, to which port they should be delivered, and preferable departure and arrival date, and how many containers.
			
			Port name is the same as city name. It should be written according English Grammar. 
			
			Confidence about departure date has to be in range from 0.0 to 1.0, where 1.0 means that a user are sure about the date, and 0.0 means that a user are not sure at all.
			Confidence about arrival date has to be in range from 0.0 to 1.0, where 1.0 means that a user are sure about the date, and 0.0 means that a user are not sure at all.
			If there is no any information about departure date, you can use (3 days after current date) as default value with confidence 0.2.
			If there is no any information about arrival date, you can use null value with confidence 0.0.
			if it is not mention year, assume that it is current year.
			If provided a date range for departure dates set the beginning of range.
			Month can be shorten -- example September can be mention as SEPT
			
			Output structure MUST be a JSON object ONLY, no any additional strings and words:
			
			{
			   "departurePort" : <Departure Port>,
			   "arrivalPort" : <Arrival Port>,
			   "departureDate" : <Departure Date in ISO format without timezone>,
			   "arrivalDate" : <Arrival Date in ISO format without timezone>,
				 "containerCount" : <number of containers to be reserved>,
				"departureConfidence" : <confidence in departure date, double number between 0.0 and 1.0>,
         "arrivalConfidence" : <confidence in arrival date, double number between 0.0 and 1.0>
      }
			
			The current date for examples is 2026-09-01.
			Example 1.
			User query: I need a reservation 5 containers from Hamburg to Gdansk. The containers has to be arrived on 25 September
			Output:
			{
			   "departurePort" : "Hamburg",
			   "arrivalPort" : "Gdansk",
			   "departureDate" : "2026-09-03",
			   "arrivalDate" : "2026-09-25",
				 "containerCount" : 5,
	       "departureConfidence" : 0.2,
				 "arrivalConfidence" : 0.9
			}
			
			Example 2.
			User query: I need a reservation 100 containers from Hamburg to Barcelona. The container will be load approximetly on  15th september, The containers must be arrived on 20 September not later
			Output:
			{
			   "departurePort" : "Hamburg",
			   "arrivalPort" : "Barcelona",
			   "departureDate" : "2026-09-15",
			   "arrivalDate" : "2026-09-25",
				 "containerCount" : 100,
	       "departureConfidence" : 0.6,
				 "arrivalConfidence" : 1.0
			}
			
			Example 3.
			User query: I want to deliver 10 containers from Gdansk to Tokio.  It is quite urgent so we need to send them before 08.09.2026
			Output:
			{
			   "departurePort" : "Gdansk",
			   "arrivalPort" : "Tokio",
			   "departureDate" : "2026-09-08",
			   "arrivalDate" : null,
				 "containerCount" : 10,
	       "departureConfidence" : 0.9,
				 "arrivalConfidence" : 0.0
			}
			
			IMPORTANT: Your response MUST be a raw JSON object only.
			Do NOT wrap it in markdown code blocks (no ```json, no ```, no backticks).
			Do NOT add any explanation, prefix, or suffix.
			Your entire response must start with '{' and end with '}'.
			""")
	@UserMessage("""
			Parse user request and extract data in JSON format
			User query: {userRequest}
			Current date: {current_date}
			""")
	@InputGuardrails(InputGuardRailService.class)
	@Agent(
			name = "MrParser",
			value = "An expert in understanding user requirements for booking container and creating structured output.",
	    outputKey = "bookingStructuredRequest"
	)
	BookingStructuredRequest reservationData(String userRequest);
}

