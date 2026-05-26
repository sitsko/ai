package me.sitsko.ai.booking;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import me.sitsko.ai.shared.security.InputGuardRailService;

@ApplicationScoped
@RegisterAiService
public interface BookingAgent {

	@SystemMessage("""
			You are a booking agent for containers in liner vessel.
			
			You should provide some solutions about suitable vessel's schedules to book containers.
			Return JSON only.
			
			Today is {current_date}.
			""")
	@InputGuardrails(InputGuardRailService.class)
	BookingResponse reservationData(String userMessage);
}

