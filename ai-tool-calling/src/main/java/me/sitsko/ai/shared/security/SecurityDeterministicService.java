package me.sitsko.ai.shared.security;

import dev.langchain4j.data.message.UserMessage;
import java.util.regex.Pattern;

public class SecurityDeterministicService {

	private static final Pattern PROHIBITED_WORDS = Pattern.compile("^.*(MSC|hack|password).*$", Pattern.CASE_INSENSITIVE);

	public boolean isAcceptable(UserMessage request) {
		return !PROHIBITED_WORDS
				.matcher(request.singleText())
				.matches();
	}
}
