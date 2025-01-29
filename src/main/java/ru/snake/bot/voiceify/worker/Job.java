package ru.snake.bot.voiceify.worker;

import ru.snake.bot.voiceify.database.Language;

public class Job {

	private final JobType type;

	private final long chatId;

	private final int messageId;

	private final String uri;

	private final String text;

	private final Language language;

	private Job(
		final JobType type,
		final long chatId,
		final int messageId,
		final String uri,
		final String text,
		final Language language
	) {
		this.type = type;
		this.chatId = chatId;
		this.messageId = messageId;
		this.uri = uri;
		this.text = text;
		this.language = language;
	}

	public JobType getType() {
		return type;
	}

	public long getChatId() {
		return chatId;
	}

	public int getMessageId() {
		return messageId;
	}

	public String getUri() {
		return uri;
	}

	public String getText() {
		return text;
	}

	public Language getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "Job [type=" + type + ", chatId=" + chatId + ", messageId=" + messageId + ", uri=" + uri + ", text="
				+ text + ", language=" + language + "]";
	}

	public static Job text(long chatId, int messageId, String text, Language language) {
		return new Job(JobType.TEXT, chatId, messageId, null, text, language);
	}

	public static Job article(long chatId, int messageId, String uri, Language language) {
		return new Job(JobType.ARTICLE, chatId, messageId, uri, null, language);
	}

	public static Job video(long chatId, int messageId, String uri, Language language) {
		return new Job(JobType.VIDEO, chatId, messageId, uri, null, language);
	}

}
