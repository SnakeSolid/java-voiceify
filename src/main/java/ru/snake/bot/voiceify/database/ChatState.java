package ru.snake.bot.voiceify.database;

import java.util.Collections;
import java.util.List;

public class ChatState {

	private final int messageId;

	private final String text;

	private final List<String> uriStrings;

	private ChatState(int messageId, String text, List<String> uriStrings) {
		this.messageId = messageId;
		this.text = text;
		this.uriStrings = uriStrings;
	}

	public int getMessageId() {
		return messageId;
	}

	public String getText() {
		return text;
	}

	public List<String> getUriStrings() {
		return uriStrings;
	}

	@Override
	public String toString() {
		return "ChatState [messageId=" + messageId + ", text=" + text + ", uriStrings=" + uriStrings + "]";
	}

	public static ChatState create(final int messageId, final String text, final List<String> uriStrings) {
		return new ChatState(messageId, text, uriStrings);
	}

	public static ChatState empty() {
		return new ChatState(-1, "", Collections.emptyList());
	}

}
