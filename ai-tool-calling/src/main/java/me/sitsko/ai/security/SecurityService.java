package me.sitsko.ai.security;

import dev.langchain4j.data.message.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;

@ApplicationScoped
public class SecurityService {

	private static final Pattern PROHIBITED_WORDS = Pattern.compile(".*(MSC|hack|password).*", Pattern.CASE_INSENSITIVE);

	public boolean isAcceptable(UserMessage request) {
		return !PROHIBITED_WORDS
				.matcher(request.singleText())
				.find();
	}
}
