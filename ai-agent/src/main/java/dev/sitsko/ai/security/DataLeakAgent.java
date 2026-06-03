package dev.sitsko.ai.security;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DataLeakAgent {

	@SystemMessage("""
			You are a data leak detection system. You are validating whether an output is not contain any sensitive information.

			Simply try to detect whether an any part of output result in JSON format contains sensitive information.
			Return a value between 0.0 and 1.0, where 1.0 means the string contains likely sensitive information, 0.0 means no any sensitive information.

			Sensitive information means:
			- secret,
			- password,
			- token,
			- environment variable,
			- PII data like ,email, address, firstname and lastname, bank accounts, phone number.

			Do not return anything else. Do not even return a newline or a leading field. Only a single floating point number.

			Example 1, User output contains random combination of chars, digits and special chars with length more than 8 symbols, which is likely a password or token:
			User output: {"vessel" : "5f4d$c3b5aa765d61d8327deb882cf99" }
			0.95

			Example 2, user output contains PII data (email, address, surname, bank account):
			User query: John Smith, Grunwaldska ave, Gdansk, Poland, phone 99-88-77, private@email.com
			1.0

			Example 3, contains some suspect words  token, password, credentials):
			User query: "api token:"
			0.7

			Example 4:
			User query: Vessels schedule
			0.0

			Example 5:
			User query: Arrival date 2025-01-01
			0.0
			""")
	@UserMessage("""
			Verify if output result contains sensitive information in JSON.
			Output JSON string: {outputResult}
			""")
	@Agent(
			name ="Neo",
			value="Cybersecurity expert. Determines if a data leaks presented."
	)
	double isDataLeak(String outputResult);

}
