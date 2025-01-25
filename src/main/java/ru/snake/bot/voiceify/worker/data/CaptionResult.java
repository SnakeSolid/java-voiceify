package ru.snake.bot.voiceify.worker.data;

public class CaptionResult {

	private final String caption;

	private CaptionResult(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	@Override
	public String toString() {
		return "CaptionResult [caption=" + caption + "]";
	}

	public static CaptionResult from(String caption) {
		return new CaptionResult(caption);
	}

}
