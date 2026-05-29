package me.sitsko.ai.security;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.sitsko.ai.exception.ProhibitedContextException;
import me.sitsko.ai.exception.PromptInjectionException;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class InputGuardRailService implements InputGuardrail {

	private static final double INJECTION_THRESHOLD = 0.7;
	private static final String PROHIBITED_ERROR_MESSAGE = "Prohibited content detected in the message";
	private static final String PROMPT_INJECTION_ERROR_MESSAGE = "Prompt injection detected, score: %.2f";

	private final SecurityAgent securityAgent;
	private final SecurityService securityService;

	@Override
	public InputGuardrailResult validate(UserMessage userMessage) {
		boolean isAccepted = securityService.isAcceptable(userMessage);

		if (!isAccepted) {
			log.warn("Deterministic test for  prompt injection FAILED, it contains prohibited words.");
			return failure(PROHIBITED_ERROR_MESSAGE, new ProhibitedContextException(PROHIBITED_ERROR_MESSAGE));
		}

		double result = securityAgent.isInjection(userMessage.singleText());
		boolean isInjected = result >= INJECTION_THRESHOLD;

		return isInjected ? createFailor(result) : createSuccess(result);
	}

	private InputGuardrailResult createSuccess(double result) {
		log.info("Non-Deterministic test for  prompt injection PASSED, score: {}.", result);;
		return success();
	}

	private InputGuardrailResult createFailor(double result) {
		log.warn("Non-Deterministic test for  prompt injection FAILED, score: {}. ", result);
		return failure("", new PromptInjectionException(formatErrorMessage(result)));
	}

	private String formatErrorMessage(double result) {
		return PROMPT_INJECTION_ERROR_MESSAGE.formatted(result);
	}
}

