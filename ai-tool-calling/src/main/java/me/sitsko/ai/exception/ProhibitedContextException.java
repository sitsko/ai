package me.sitsko.ai.exception;

public class ProhibitedContextException extends RuntimeException {

	public ProhibitedContextException(String prohibitedError) {
		super(prohibitedError);
	}
}
