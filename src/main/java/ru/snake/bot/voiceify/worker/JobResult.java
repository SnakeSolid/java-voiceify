package ru.snake.bot.voiceify.worker;

import java.io.File;

public class JobResult {

	private final boolean success;

	private final String text;

	private final File speechPath;

	private final String message;

	public JobResult(final boolean success, final String text, final File speechPath, final String message) {
		this.success = success;
		this.text = text;
		this.speechPath = speechPath;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getText() {
		return text;
	}

	public File getSpeechPath() {
		return speechPath;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "JobResult [success=" + success + ", text=" + text + ", speechPath=" + speechPath + ", message="
				+ message + "]";
	}

}
