package ru.snake.bot.voiceify.worker.data;

public class ArticleResult {

	private final String title;

	private final String text;

	private ArticleResult(String title, String text) {
		this.title = title;
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "ArticleResult [title=" + title + ", text=" + text + "]";
	}

	public static ArticleResult from(final String title, final String text) {
		return new ArticleResult(title, text);
	}

}
