package ru.snake.bot.voiceify.worker.data;

public class SubtitlesResult {

	private final boolean success;

	private final String title;

	private final String subtitles;

	private SubtitlesResult(final boolean success, final String title, final String subtitles) {
		this.success = success;
		this.title = title;
		this.subtitles = subtitles;
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

	@Override
	public String toString() {
		return "SubtitlesResult [success=" + success + ", title=" + title + ", subtitles=" + subtitles + "]";
	}

	public static SubtitlesResult success(final String title, final String subtitles) {
		return new SubtitlesResult(true, title, subtitles);
	}

	public static SubtitlesResult fail() {
		return new SubtitlesResult(false, null, null);
	}

}
