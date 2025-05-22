package ru.snake.bot.voiceify.worker;

import ru.snake.bot.voiceify.database.UserSettings;

public class Job {

	private final JobType type;

	private final long chatId;

	private final int messageId;

	private final String uri;

	private final String text;

	private final UserSettings settings;

	private Job(
		final JobType type,
		final long chatId,
		final int messageId,
		final String uri,
		final String text,
		final UserSettings settings
	) {
		this.type = type;
		this.chatId = chatId;
		this.messageId = messageId;
		this.uri = uri;
		this.text = text;
		this.settings = settings;
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

	public UserSettings getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		return "Job [type=" + type + ", chatId=" + chatId + ", messageId=" + messageId + ", uri=" + uri + ", text="
				+ text + ", settings=" + settings + "]";
	}

	public static Job text(long chatId, int messageId, String text, UserSettings settings) {
		return new Job(JobType.TEXT, chatId, messageId, null, text, settings);
	}

	public static Job subtitles(long chatId, int messageId, String text, UserSettings settings) {
		return new Job(JobType.SUBTITLES, chatId, messageId, null, text, settings);
	}

	public static Job article(long chatId, int messageId, String uri, UserSettings settings) {
		return new Job(JobType.ARTICLE, chatId, messageId, uri, null, settings);
	}

	public static Job video(long chatId, int messageId, String uri, UserSettings settings) {
		return new Job(JobType.VIDEO, chatId, messageId, uri, null, settings);
	}

}
