package me.sitsko.ai.vessel;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrails;
import jakarta.enterprise.context.ApplicationScoped;
import me.sitsko.ai.tool.VesselTool;

/**
 * @author Mikalai Sitsko , 06/27/2025
 */
@RegisterAiService(tools = {VesselTool.class})
@ApplicationScoped
public interface VesselAiService {

	@UserMessage("""
    Return the vessel which has maximum TEU size
    """)
	@OutputGuardrails(VesselGuardrail.class)
	Vessel getHeavyVessel();
}
