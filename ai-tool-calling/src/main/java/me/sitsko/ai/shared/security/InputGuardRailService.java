package me.sitsko.ai.shared.security;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class InputGuardRailService implements InputGuardrail {

	public static final double INJECTION_THRESHOLD = 0.7;
	private final SecurityAgent securityAgent;
	private final SecurityDeterministicService securityService;

	@Override
	public InputGuardrailResult validate(UserMessage userMessage) {
		boolean isAccepted = securityService.isAcceptable(userMessage);

		if (!isAccepted) {
			return failure("Prohibited content detected in the message");
		}

		double result = securityAgent.isInjection(userMessage.singleText());
		boolean isInjected = result >= INJECTION_THRESHOLD;

		return isInjected ? failure("Prompt injection detected, score: " + result) : success();
	}
}

