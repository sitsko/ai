package me.sitsko.ai.vessel;

import dev.langchain4j.data.message.AiMessage;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrail;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mikalai Sitsko , 06/27/2025
 */
@ApplicationScoped
public class VesselGuardrail implements OutputGuardrail {

	private final Pattern PATTERN = Pattern.compile("dummy");

	@Override
	public OutputGuardrailResult validate(AiMessage responseFromLLM) {
		try {
			Matcher matcher = PATTERN.matcher(responseFromLLM.text());
			return !matcher.find() ? success() : failure("dummy vessel ");
		} catch (Exception e) {
			return reprompt("Invalid text format", e, "Make sure you return a valid requested text");
		}
	}
}
