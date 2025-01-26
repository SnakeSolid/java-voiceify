package ru.snake.bot.voiceify.worker.data;

public class SubtitlesResult {

	private final boolean success;

	private final String title;

	private final String subtitles;

	private final String message;

	private SubtitlesResult(boolean success, final String title, final String subtitles, final String message) {
		this.success = success;
		this.title = title;
		this.subtitles = subtitles;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitles() {
		return subtitles;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "SubtitlesResult [success=" + success + ", title=" + title + ", subtitles=" + subtitles + ", message="
				+ message + "]";
	}

	public static SubtitlesResult success(final String title, final String subtitles) {
		return new SubtitlesResult(true, title, subtitles, null);
	}

	public static SubtitlesResult fail(final String message) {
		return new SubtitlesResult(false, null, null, message);
	}

}
