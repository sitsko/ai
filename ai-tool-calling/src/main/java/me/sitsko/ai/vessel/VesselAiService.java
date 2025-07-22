package me.sitsko.ai.vessel;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrails;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.temporal.ChronoUnit;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * AI service for handling vessel-related operations.
 * @author Mikalai Sitsko , 06/27/2025
 */
@RegisterAiService()
@ApplicationScoped
@SystemMessage("""
		You are a  maritime container shipping expert.
		Your information based on data from official websites and wikipedia
		""")
public interface VesselAiService {

	@UserMessage("""
    Return the vessel which has maximum TEU size from the vessel {owner}
    """)
	@ToolBox(VesselTool.class)
	@OutputGuardrails(VesselGuardrail.class)
	Vessel getHeavyVessel(String owner);

	@UserMessage("""
    Return the number of vessels operated by {company}
    """)
	int countVessels(String company);

	@UserMessage("""
		Generate report how many vessels are going to be produces in the next {years} years.
		Group results by owner and return the list of vessels.
		""")
	@Timeout(value = 60, unit = ChronoUnit.SECONDS)
//	@Fallback(fallbackMethod = "fallback")
	@RunOnVirtualThread
	String generateForecast(int years);

//	default String fallback() {
//
//		return "Ops";
//	}
}
