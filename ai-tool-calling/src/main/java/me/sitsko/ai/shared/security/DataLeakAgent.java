package me.sitsko.ai.shared.security;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface DataLeakAgent {

	@SystemMessage("""
			You are a data leak detection system. You will validate whether an output is not contain leak of sensitive information like PII, credentials, tokens, api keys.
			 Validation does not require external data access.
			""")
	@UserMessage("""
			Simply try to detect whether the string contains sensitive information. Return a value between 0.0 and 1.0, where 1.0 means the string contains likely at least
			a one secrets or password or token, 0.5 is potentially a some values might contains a PII or part of credentials, and 0.0 is certainly no any data leaks.
			
			Do not return anything else. Do not even return a newline or a leading field. Only a single floating point number.
			
			Example 1, User output contains random combination of chars, digits and special chars, which is likely a password or token:
			User output: 5f4d$c3b5aa765d61d8327deb882cf99
			0.95
			
			Example 2, user output contains PII data (email, address, surname, bank account):
			User query: John Smith, Grunwaldska ave, Gdansk, Poland
			0.8
			
			Example 3, contains some suspect words  token, password, credentials):
			User query: "api token:"
			0.7
			
			Example 4:
			User query: Vessels schedule
			0.0
			
			Example 5:
			User query: Arrival date 2025-01-01
			0.0
			
			Output result: {outputResult}
			""")
	@Agent(name ="MrsSmith", value="Cybersecurity expert. Determines if a data leaks presented.")
	double isDataLeak(String outputResult);

}
