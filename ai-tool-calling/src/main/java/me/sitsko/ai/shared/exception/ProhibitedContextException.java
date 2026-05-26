package me.sitsko.ai.shared.exception;

public class ProhibitedContextException extends RuntimeException {

	public ProhibitedContextException(String prohibitedError) {
		super(prohibitedError);
	}
}
