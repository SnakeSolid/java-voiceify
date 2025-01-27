package ru.snake.bot.voiceify.worker.data;

public class SubtitlesResult {

	private final String title;

	private final String subtitles;

	public SubtitlesResult(final String title, final String subtitles) {
		this.title = title;
		this.subtitles = subtitles;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitles() {
		return subtitles;
	}

	@Override
	public String toString() {
		return "SubtitlesResult [title=" + title + ", subtitles=" + subtitles + "]";
	}

}
