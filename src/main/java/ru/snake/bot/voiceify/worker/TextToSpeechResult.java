package ru.snake.bot.voiceify.worker;

import java.io.File;

public class TextToSpeechResult {

	private final boolean success;

	private final File speechPath;

	private final String message;

	private TextToSpeechResult(boolean success, File speechPath, String message) {
		this.success = success;
		this.speechPath = speechPath;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public File getSpeechPath() {
		return speechPath;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "TextToSpeechResult [success=" + success + ", speechPath=" + speechPath + ", message=" + message + "]";
	}

	public static TextToSpeechResult success(File speechPath) {
		return new TextToSpeechResult(true, speechPath, null);
	}

	public static TextToSpeechResult fail(String message) {
		return new TextToSpeechResult(false, null, message);
	}

}
