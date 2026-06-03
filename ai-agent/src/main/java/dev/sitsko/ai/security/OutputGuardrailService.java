package dev.sitsko.ai.security;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.sitsko.ai.exception.DataLeakException;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class OutputGuardrailService implements OutputGuardrail {

	private static final double DATA_LEAK_THRESHOLD = 0.7;
	private static final String POTENTIAL_DATA_LEAK_ERROR_MESSAGE = "Potential data leak detected, score: %.2f";

	private final DataLeakAgent leakAgent;

	@Override
	public OutputGuardrailResult validate(AiMessage responseFromLLM) {

		double dataLeakScore = leakAgent.isDataLeak(responseFromLLM.text());
		boolean isDataLeak = dataLeakScore >= DATA_LEAK_THRESHOLD;

		log.info("Data leak score: {}", dataLeakScore);

		return isDataLeak ? failure("", new DataLeakException(formatErrorMessage(dataLeakScore))) : success();
	}

	private String formatErrorMessage(double dataLeakScore) {
		return POTENTIAL_DATA_LEAK_ERROR_MESSAGE.formatted(dataLeakScore);
	}
}
