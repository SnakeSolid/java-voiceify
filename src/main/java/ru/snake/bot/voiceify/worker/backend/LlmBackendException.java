package ru.snake.bot.voiceify.worker.backend;

public class LlmBackendException extends Exception {

	private static final long serialVersionUID = 9120825221990323575L;

	public LlmBackendException(String message) {
		super(message);
	}

	public LlmBackendException(String message, Throwable cause) {
		super(message, cause);
	}

	public LlmBackendException(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return "LlmBackendException []";
	}

}
