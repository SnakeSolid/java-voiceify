package ru.snake.bot.voiceify.worker;

import java.io.File;

public class JobResult {

	private final boolean success;

	private final String caption;

	private final File speechPath;

	private final String message;

	public JobResult(final boolean success, final String caption, final File speechPath, final String message) {
		this.success = success;
		this.caption = caption;
		this.speechPath = speechPath;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getCaption() {
		return caption;
	}

	public File getSpeechPath() {
		return speechPath;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "JobResult [success=" + success + ", caption=" + caption + ", speechPath=" + speechPath + ", message="
				+ message + "]";
	}

}
