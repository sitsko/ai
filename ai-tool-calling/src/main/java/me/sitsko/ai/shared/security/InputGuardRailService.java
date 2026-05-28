package me.sitsko.ai.shared.security;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.sitsko.ai.shared.exception.ProhibitedContextException;
import me.sitsko.ai.shared.exception.PromptInjectionException;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class InputGuardRailService implements InputGuardrail {

	private static final double INJECTION_THRESHOLD = 0.7;
	private static final String PROHIBITED_ERROR_MESSAGE = "Prohibited content detected in the message";
	private static final String PROMPT_INJECTION_ERROR_MESSAGE = "Prompt injection detected, score: %.2f";

	private final SecurityAgent securityAgent;
	private final SecurityDeterministicService securityService;

	@Override
	public InputGuardrailResult validate(UserMessage userMessage) {
		boolean isAccepted = securityService.isAcceptable(userMessage);

		if (!isAccepted) {
			return failure(PROHIBITED_ERROR_MESSAGE, new ProhibitedContextException(PROHIBITED_ERROR_MESSAGE));
		}

		double result = securityAgent.isInjection(userMessage.singleText());
		log.info("Prompt injection score: {}", result);

		boolean isInjected = result >= INJECTION_THRESHOLD;

		return isInjected ? failure("", new PromptInjectionException(formatErrorMessage(result))) : success();
	}

	private String formatErrorMessage(double result) {
		return PROMPT_INJECTION_ERROR_MESSAGE.formatted(result);
	}
}

