package me.sitsko.ai.vessel;

import io.quarkiverse.langchain4j.guardrails.InputGuardrail;
import io.quarkiverse.langchain4j.guardrails.InputGuardrailParams;
import io.quarkiverse.langchain4j.guardrails.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Input guardrail for validating input parameters for vessel-related operations.
 * It checks if the owner is not null, not blank, and does not contain a rude word.
 * @author Mikalai Sitsko , 06/27/2025
 */
@Slf4j
@ApplicationScoped
public class VesselInputGuardRail implements InputGuardrail {

	public static final String BAD_WORD = "bad";

	@Override
	public InputGuardrailResult validate(InputGuardrailParams params) {
		try {
			String owner = (String) params.variables().get("owner");
			if (owner == null || owner.isBlank() || owner.equals(BAD_WORD)) {
				return failure("Used a rude word");
			}
			return success();
		} catch (Exception e) {
			log.error("Error validating input parameters. Exception: {} ", e.getMessage(), e);
			return failure("Ooops");
		}
	}

}
