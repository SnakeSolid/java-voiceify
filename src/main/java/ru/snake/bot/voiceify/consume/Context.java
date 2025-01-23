package ru.snake.bot.voiceify.consume;

public class Context {

	private final long userId;

	private final long chatId;

	private final int messageId;

	private Context(long userId, long chatId, int messageId) {
		this.userId = userId;
		this.chatId = chatId;
		this.messageId = messageId;
	}

	public long getUserId() {
		return userId;
	}

	public long getChatId() {
		return chatId;
	}

	public int getMessageId() {
		return messageId;
	}

	@Override
	public String toString() {
		return "Context [userId=" + userId + ", chatId=" + chatId + ", messageId=" + messageId + "]";
	}

	public static Context from(final long userId, final long chatId, final int messageId) {
		return new Context(userId, chatId, messageId);
	}

}
